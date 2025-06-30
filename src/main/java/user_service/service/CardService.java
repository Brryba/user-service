package user_service.service;

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
public class CardService {
    private final UserDao userDao;
    private final CardDao cardDao;
    private final CardMapper cardMapper;

    public CardService(UserDao userDao, CardDao cardDao, CardMapper cardMapper) {
        this.userDao = userDao;
        this.cardDao = cardDao;
        this.cardMapper = cardMapper;
    }

    private void validateCardNumberUnique(CardRequestDto cardRequestDto) throws CardNumberNotUniqueException {
        String number = cardRequestDto.getNumber().describeConstable()
                .orElseThrow(CardNumberNotUniqueException::new);
    }

    private User getCardOwnerUserById(CardRequestDto cardRequestDto) throws UserNotFoundException {
        final long userId = cardRequestDto.getUserId();
        User user = userDao.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        return user;
    }

    public CardResponseDto createCard(CardRequestDto cardRequestDto) {
        validateCardNumberUnique(cardRequestDto);
        Card card = cardMapper.toCard(cardRequestDto);
        card.setUser(getCardOwnerUserById(cardRequestDto));
        cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    public CardResponseDto getCardById(Long id) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
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
    public CardResponseDto updateCard(CardRequestDto cardRequestDto, long id) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        if (!cardRequestDto.getNumber().equals(card.getNumber())) {
            validateCardNumberUnique(cardRequestDto);
        }

        cardMapper.updateCardFromDto(cardRequestDto, card);
        if (!card.getUser().getId().equals(cardRequestDto.getUserId())) {
            card.setUser(getCardOwnerUserById(cardRequestDto));
        }

        cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardDao.delete(card);
    }
}
