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
import user_service.exception.card.CardNotFoundException;
import user_service.exception.card.CardNumberNotUniqueException;
import user_service.exception.card.CardsNotFoundException;
import user_service.exception.card.InvalidCardOwnerException;
import user_service.exception.user.UserNotFoundException;
import user_service.mapper.CardMapper;

import java.util.List;

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

    private User getCurrentUser(Long userId) {
        return userDao.findUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private boolean isAuthenticatedUserNotCardOwner(Card card, long currentUserId) {
        return card.getUser().getId() != currentUserId;
    }

    @CachePut(value = "card:id", key = "#result.id")
    @CacheEvict(value = "user:id", key = "#result.userId")
    public CardResponseDto createCard(CardRequestDto cardRequestDto, Long userId) {
        validateCardNumberUnique(cardRequestDto);
        Card card = cardMapper.toCard(cardRequestDto);
        card.setUser(getCurrentUser(userId));
        card = cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    @Cacheable("card:id")
    public CardResponseDto getCardById(Long id, Long userId) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        if (isAuthenticatedUserNotCardOwner(card, userId)) {
            throw new InvalidCardOwnerException("The card is owned by another user");
        }
        return cardMapper.toResponseDto(card);
    }

    @Transactional
    @Caching(put = {
            @CachePut(value = "card:id", key = "#id")
    }, evict = {
            @CacheEvict(value = "user:id", key = "#result.userId")
    })
    public CardResponseDto updateCard(CardRequestDto cardRequestDto, long id, Long userId) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        if (!cardRequestDto.getNumber().equals(card.getNumber())) {
            validateCardNumberUnique(cardRequestDto);
        }

        cardMapper.updateCardFromDto(cardRequestDto, card);
        if (isAuthenticatedUserNotCardOwner(card, userId)) {
            throw new InvalidCardOwnerException("The card is owned by another user");
        }

        cardDao.save(card);
        return cardMapper.toResponseDto(card);
    }

    public List<CardResponseDto> getCardsByIds(List<Long> ids, Long currentUserId) {
        List<Card> cards = cardDao.findCardsByIdIn(ids);
        if (cards == null || cards.isEmpty()) {
            throw new CardsNotFoundException();
        }
        return cards
                .stream()
                .map(cardMapper::toResponseDto)
                .peek(card -> {
                    if (!card.getUserId().equals(currentUserId)) {
                        throw new InvalidCardOwnerException("The card is owned by another user");
                    }
                })
                .toList();
    }

    @Transactional
    @CacheEvict(value = "card:id", key = "#id")
    public void deleteCard(Long id, Long userId) {
        Card card = cardDao.findCardById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (isAuthenticatedUserNotCardOwner(card, userId)) {
            throw new InvalidCardOwnerException("The card is owned by another user");
        }
        cacheManager.getCache("user:id").evict(card.getUser().getId());
        cardDao.delete(card);
    }
}
