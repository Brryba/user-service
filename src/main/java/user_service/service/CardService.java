package user_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user_service.dao.CardDao;
import user_service.dao.UserDao;
import user_service.dto.card.BaseCardRequestDto;
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
public class CardService {
    private final UserDao userDao;
    private final CardDao cardDao;
    private final CardMapper cardMapper;

    public CardService(UserDao userDao, CardDao cardDao, CardMapper cardMapper) {
        this.userDao = userDao;
        this.cardDao = cardDao;
        this.cardMapper = cardMapper;
    }

    private void validateCardInfo(BaseCardRequestDto cardRequestDto) {
        String number = cardRequestDto.getNumber();
        if (cardDao.findCardByNumber(number) != null) {
            throw new CardNumberNotUniqueException();
        }

        long userId = cardRequestDto.getUserId();
        User user = userDao.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
    }

    public CardResponseDto createCard(BaseCardRequestDto cardRequestDto) {
        validateCardInfo(cardRequestDto);

        Card card = cardMapper.toCard(cardRequestDto);
        cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    public CardResponseDto getCardById(Long id) {
        Card card = cardDao.findCardById(id);
        if (card == null) {
            throw new CardNotFoundException(id);
        }
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
    public CardResponseDto updateCard(BaseCardRequestDto cardRequestDto) {
        validateCardInfo(cardRequestDto);

        Card card = cardMapper.toCard(cardRequestDto);
        cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    @Transactional
    public CardResponseDto deleteCard(Long id) {
        Card card = cardDao.findCardById(id);
        if (card == null) {
            throw new CardNotFoundException(id);
        }

        cardDao.delete(card);
        return cardMapper.toResponseDto(card);
    }
}
