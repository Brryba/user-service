package user_service.card;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import user_service.mapper.CardMapperImpl;
import user_service.mapper.UserMapperImpl;
import user_service.service.UserService;

@EnableCaching
@SpringBootTest(classes = {UserService.class,
        UserMapperImpl.class,
        CardMapperImpl.class,
        CardServiceCacheTests.class})
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
    private UserService userService;


}
