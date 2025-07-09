package user_service.unit_tests.card;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import user_service.dto.card.CardResponseDto;
import user_service.entity.Card;
import user_service.entity.User;
import user_service.mapper.CardMapperImpl;
import user_service.mapper.UserMapperImpl;
import user_service.service.CardService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableCaching
@SpringBootTest(classes = {CardService.class,
        UserMapperImpl.class,
        CardMapperImpl.class,
        CardServiceCacheTests.CacheTestConfig.class})
public class CardServiceCacheTests extends CardServiceBaseTests {
    @Configuration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CardService cardService;

    @AfterEach
    void clearCache() {
        cacheManager.getCache("user:id").clear();
        cacheManager.getCache("card:id").clear();
    }

    @Test
    public void getCardById_shouldCacheResult() {
        when(cardDao.findCardById(1L)).thenReturn(Optional.ofNullable(card));

        CardResponseDto responseDto = cardService.getCardById(1L);
        Cache cardCache = cacheManager.getCache("card:id");
        long id = responseDto.getId();

        assertThat(cardCache).isNotNull();
        assertThat(cardCache.get(id).get()).isNotNull();
        assertThat(cardCache.get(id).get()).isSameAs(responseDto);
    }

    @Test
    public void getCardById_shouldUseCache() {
        when(cardDao.findCardById(1L)).thenReturn(Optional.ofNullable(card));

        CardResponseDto responseDto = cardService.getCardById(1L);
        CardResponseDto responseDto2 = cardService.getCardById(1L);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto2).isNotNull();
        assertThat(responseDto).isEqualTo(responseDto2);
        verify(cardDao, times(1)).findCardById(1L);
    }

    @Test
    public void createCard_shouldCacheResult() {
        when(cardDao.save(any(Card.class))).thenReturn(card);
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));

        CardResponseDto responseDtoAfterCreate = cardService.createCard(cardRequestDto);
        Cache cardCache = cacheManager.getCache("card:id");
        long id = responseDtoAfterCreate.getId();

        assertThat(cardCache).isNotNull();
        assertThat(cardCache.get(id).get()).isNotNull();
        assertThat(cardCache.get(id).get()).isEqualTo(responseDtoAfterCreate);
    }

    @Test
    public void updateCard_shouldUpdateCacheResult() {
        when(cardDao.save(any(Card.class))).thenReturn(card);
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));

        CardResponseDto responseDtoAfterCreate = cardService.createCard(cardRequestDto);
        long id = responseDtoAfterCreate.getId();
        when(cardDao.findCardById(id)).thenReturn(Optional.of(card));

        cardRequestDto.setNumber("7766554433221100");
        CardResponseDto responseDtoAfterModify = cardService.updateCard(cardRequestDto, id);

        Cache cardCache = cacheManager.getCache("card:id");
        assertThat(cardCache).isNotNull();
        assertThat(cardCache.get(id).get()).isNotNull();
        assertThat(cardCache.get(id).get()).isEqualTo(responseDtoAfterModify);
        assertThat(cardCache.get(id).get()).isNotEqualTo(responseDtoAfterCreate);
    }

    @Test
    public void deleteCard_shouldDeleteCacheResult() {
        when(cardDao.findCardById(1L)).thenReturn(Optional.of(card));
        cardService.deleteCard(1L);

        Cache cardCache = cacheManager.getCache("card:id");
        if (cardCache != null) {
            assertThat(cardCache.get(1L)).isNull();
        }
    }

    @Test
    public void createCard_evictsUserCacheResult() {
        when(cardDao.save(any(Card.class))).thenReturn(card);
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));

        cacheManager.getCache("card:id").put(1L, "Some mock data");
        cardService.createCard(cardRequestDto);

        Cache userCache = cacheManager.getCache("user:id");
        if (userCache != null) {
            assertThat(userCache.get(1L)).isNull();
        }
    }

    @Test
    public void updateCard_evictsUserCacheResult() {
        when(cardDao.save(any(Card.class))).thenReturn(card);
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));
        when(cardDao.findCardById(1L)).thenReturn(Optional.ofNullable(card));

        cacheManager.getCache("card:id").put(1L, "Some mock data");
        cardRequestDto.setNumber("7766554433221100");
        cardService.updateCard(cardRequestDto, 1L);

        Cache userCache = cacheManager.getCache("user:id");
        if (userCache != null) {
            assertThat(userCache.get(1L)).isNull();
        }
    }

    @Test
    public void updateCardUser_evictsBothUsersCacheResult() {
        when(cardDao.save(any(Card.class))).thenReturn(card);
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(cardUser));
        when(cardDao.findCardById(1L)).thenReturn(Optional.ofNullable(card));

        User cardUser2 = buildUser();
        cardUser2.setId(2L);
        when(userDao.findUserById(2L)).thenReturn(Optional.of(cardUser2));

        cacheManager.getCache("card:id").put(1L, "Some mock data");
        cacheManager.getCache("card:id").put(2L, "Some other mock data");
        cardRequestDto.setUserId(2L);
        cardService.updateCard(cardRequestDto, 1L);

        Cache userCache = cacheManager.getCache("user:id");
        if (userCache != null) {
            assertThat(userCache.get(1L)).isNull();
            assertThat(userCache.get(2L)).isNull();
        }
    }

    @Test
    public void deleteCard_evictsUserCacheResult() {
        when(cardDao.findCardById(1L)).thenReturn(Optional.ofNullable(card));
        doNothing().when(cardDao).delete(any(Card.class));

        cacheManager.getCache("card:id").put(1L, "Some mock data");
        cardService.deleteCard(1L);

        Cache userCache = cacheManager.getCache("user:id");
        if (userCache != null) {
            assertThat(userCache.get(1L)).isNull();
        }
    }
}
