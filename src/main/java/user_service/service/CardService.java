package user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user_service.dao.CardDao;
import user_service.dao.UserDao;
import user_service.dto.card.CardRequestDto;
import user_service.dto.card.CardResponseDto;
import user_service.entity.Card;
import user_service.entity.User;
import user_service.exception.CardNotFoundException;
import user_service.exception.CardNumberNotUniqueException;
import user_service.exception.CardsNotFoundException;
import user_service.exception.UserNotFoundException;
import user_service.mapper.CardMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    private final UserDao userDao;
    private final CardDao cardDao;
    private final CardMapper cardMapper;
    private final CacheManager cacheManager;

    private void validateCardNumberUnique(CardRequestDto cardRequestDto) throws CardNumberNotUniqueException {
        String number = cardRequestDto.getNumber();
        if (cardDao.findCardByNumber(number).isPresent()) {
            throw new CardNumberNotUniqueException();
        }
    }

    private User getCardOwnerUserById(CardRequestDto cardRequestDto) throws UserNotFoundException {
        final long userId = cardRequestDto.getUserId();
        return userDao.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @CachePut(value = "card:id", key = "#result.id")
    @CacheEvict(value = "user:id", key = "#cardRequestDto.userId")
    public CardResponseDto createCard(CardRequestDto cardRequestDto) {
        validateCardNumberUnique(cardRequestDto);
        Card card = cardMapper.toCard(cardRequestDto);
        card.setUser(getCardOwnerUserById(cardRequestDto));
        card = cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    @Cacheable("card:id")
    public CardResponseDto getCardById(Long id) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.toResponseDto(card);
    }

    @Transactional
    @Caching(put = {
            @CachePut(value = "card:id", key = "#id")
    }, evict = {
            @CacheEvict(value = "user:id", key = "#cardRequestDto.userId")
    })
    public CardResponseDto updateCard(CardRequestDto cardRequestDto, long id) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        if (!cardRequestDto.getNumber().equals(card.getNumber())) {
            validateCardNumberUnique(cardRequestDto);
        }

        cardMapper.updateCardFromDto(cardRequestDto, card);
        long previousUserId = card.getUser().getId();
        if (previousUserId != cardRequestDto.getUserId()) {
            cacheManager.getCache("user:id").evict(previousUserId);
            card.setUser(getCardOwnerUserById(cardRequestDto));
        }

        cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    public List<CardResponseDto> getCardsByIds(List<Long> ids) {
        List<Card> cards = cardDao.findCardsByIdIn(ids);
        if (cards == null || cards.isEmpty()) {
            throw new CardsNotFoundException();
        }
        return cards
                .stream()
                .map(cardMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "card:id", key = "#id")
    public void deleteCard(Long id) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cacheManager.getCache("user:id").evict(card.getUser().getId());
        cardDao.delete(card);
    }
}
