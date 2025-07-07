package user_service.card;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user_service.dao.UserDao;
import user_service.dto.card.CardRequestDto;
import user_service.dto.card.CardResponseDto;
import user_service.entity.Card;
import user_service.exception.CardNotFoundException;
import user_service.exception.CardNumberNotUniqueException;
import user_service.exception.CardsNotFoundException;
import user_service.exception.UserNotFoundException;
import user_service.mapper.CardMapperImpl;
import user_service.service.CardService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CardService.class,
        CardMapperImpl.class,})
public class CardServiceLayerUnitTests extends CardServiceBaseTests {
    @Autowired
    private CardService cardService;

    @MockitoBean
    private CacheManager cacheManager;
    @Autowired
    private UserDao userDao;

    private boolean requestResponseEquals(CardRequestDto request, CardResponseDto response) {
        return request.getUserId().equals(response.getUserId()) &&
                request.getHolder().equals(response.getHolder())
                && request.getNumber().equals(response.getNumber())
                && request.getExpirationDate().equals(response.getExpirationDate());
    }

    @Test
    public void createCard_success() {
        given(cardDao.save(any(Card.class))).willReturn(card);
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));

        CardResponseDto cardResponseDto = cardService.createCard(cardRequestDto);

        assertThat(cardResponseDto).isNotNull();
        assertThat(requestResponseEquals(cardRequestDto, cardResponseDto)).isTrue();
    }

    @Test
    public void createCard_cardNumberNotUnique_throwsException() {
        when(cardDao.findCardByNumber("001122334455667788"))
                .thenReturn(Optional.of(card));
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));

        assertThrows(CardNumberNotUniqueException.class,
                () -> cardService.createCard(cardRequestDto));
    }

    @Test
    public void createCard_cardOwnerUserIdNotExists_throwsException() {
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));
        when(cardDao.save(any(Card.class))).thenReturn(card);
        cardRequestDto.setUserId(12345L);

        assertThrows(UserNotFoundException.class,
                () -> cardService.createCard(cardRequestDto));
    }

    @Test
    public void getCardById_success() {
        when(cardDao.findCardById(1L)).thenReturn(Optional.ofNullable(card));

        CardResponseDto cardResponseDto = cardService.getCardById(1L);

        assertThat(cardResponseDto).isNotNull();
        assertThat(cardResponseDto.getId()).isEqualTo(1L);
        assertThat(requestResponseEquals(cardRequestDto, cardResponseDto)).isTrue();
    }

    @Test
    public void getCardById_cardNotExists_throwsException() {
        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(12345L),
                "Card with id " + 12345L + " does not exist");
    }

    @Test
    public void getCardsByIds_success() {
        Card card2 = buildCard();
        card2.setId(2L);

        when(cardDao.findCardsByIdIn(List.of(1L, 2L))).thenReturn(List.of(card, card2));

        List<CardResponseDto> cards = cardService.getCardsByIds(List.of(1L, 2L));

        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(2);
        assertThat(cards.getFirst().getId()).isEqualTo(1L);
        assertThat(requestResponseEquals(cardRequestDto, cards.getFirst())).isTrue();
        assertThat(cards.get(1).getId()).isEqualTo(2L);
        assertThat(requestResponseEquals(cardRequestDto, cards.get(1))).isTrue();
    }

    @Test
    public void getAllCardsByIdsTest_notAllCardsWithIdsExist_success() {
        given(cardDao.findCardsByIdIn(List.of(1L, 2L))).willReturn(List.of(card));

        List<CardResponseDto> cards = cardService.getCardsByIds(List.of(1L, 2L));

        assertThat(cards).hasSize(1);
        assertThat(cards.getFirst().getId()).isEqualTo(1L);
        assertThat(requestResponseEquals(cardRequestDto, cards.getFirst())).isTrue();
    }

    @Test
    public void getAllCardsByIdsTest_noCardsFound_throwsException() {
        assertThrows(CardsNotFoundException.class,
                () -> cardService.getCardsByIds(List.of(12345L, 67890L)));
    }

    @Test
    public void updateCard_success() {

    }
}
