package user_service.unit_tests.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;
import user_service.mapper.CardMapperImpl;
import user_service.mapper.UserMapperImpl;
import user_service.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableCaching
@SpringBootTest(classes = {UserService.class,
        UserMapperImpl.class,
        CardMapperImpl.class,
        UserServiceCacheTests.CacheTestConfig.class,})
public class UserServiceCacheTests extends UserServiceBaseTests {
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
    private UserService userService;

    @Test
    public void getUserById_shouldCacheResult() {
        when(userDao.findUserById(1L)).thenReturn(Optional.of(user));

        UserResponseDto firstResult = userService.getUserById(1L);

        Cache userCache = cacheManager.getCache("user:id");
        assertThat(userCache).isNotNull();
        assertThat(userCache.get(1L).get()).isEqualTo(firstResult);
    }

    @Test
    public void getUserById_useCachedResult() {
        when(userDao.findUserById(1L)).thenReturn(Optional.of(user));

        UserResponseDto firstResult = userService.getUserById(1L);
        UserResponseDto secondResult = userService.getUserById(1L);

        verify(userDao, times(1)).findUserById(1L);
        assertThat(firstResult).isNotNull();
        assertThat(secondResult).isNotNull();
        assertThat(firstResult).isEqualTo(secondResult);
    }

    @Test
    public void createUser_shouldCacheResult() {
        when(userDao.save(any(User.class))).thenReturn(user);

        UserResponseDto responseDtoAfterCreate = userService.createUser(userRequestDto);
        Cache userCache = cacheManager.getCache("user:id");
        long id = responseDtoAfterCreate.getId();

        assertThat(userCache).isNotNull();
        assertThat(userCache.get(id).get()).isNotNull();
        assertThat(userCache.get(id).get()).isEqualTo(responseDtoAfterCreate);
    }

    @Test
    public void updateUser_shouldUpdateCacheResult() {
        when(userDao.save(any(User.class))).thenReturn(user);

        UserResponseDto responseDtoAfterCreate = userService.createUser(userRequestDto);
        long id = responseDtoAfterCreate.getId();
        when(userDao.findUserById(id)).thenReturn(Optional.of(user));

        userRequestDto.setName("Another Name");
        UserResponseDto responseDtoAfterModify = userService.updateUser(userRequestDto, id);

        Cache userCache = cacheManager.getCache("user:id");
        assertThat(userCache).isNotNull();
        assertThat(userCache.get(id).get()).isNotNull();
        assertThat(userCache.get(id).get()).isEqualTo(responseDtoAfterModify);
        assertThat(userCache.get(id).get()).isNotEqualTo(responseDtoAfterCreate);
    }

    @Test
    public void deleteUser_shouldDeleteCacheResult() {
        when(userDao.findUserById(1L)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);

        Cache userCache = cacheManager.getCache("user:id");
        if (userCache != null) {
            assertThat(userCache.get(1L)).isNull();
        }
    }
}
