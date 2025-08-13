package user_service.integration.testContainers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import user_service.dao.CardDao;
import user_service.dto.card.CardRequestDto;
import user_service.dto.card.CardResponseDto;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.Card;
import user_service.exception.CardNumberNotUniqueException;
import user_service.exception.UserNotFoundException;
import user_service.mapper.CardMapper;
import user_service.service.CardService;
import user_service.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableCaching
@Testcontainers
public class CardTestContainerTests {
    @Autowired
    private CardDao cardDao;

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CardMapper cardMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );


    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
                    .withExposedPorts(6379);

    private static CardRequestDto cardRequestDto;

    private static long userId;

    @BeforeAll
    static void beforeAll(@Autowired UserService userService) {
        postgres.start();
        redis.start();

        userId = userService.createUser(UserRequestDto.builder()
                        .name("test")
                        .surname("test")
                        .email("test@test.com")
                        .birthDate(LocalDate.now().minusYears(1))
                        .build(), 1L)
                .getId();
    }

    @BeforeEach
    public void beforeEach() {
        cardRequestDto = CardRequestDto.builder()
                .number("0011223344556677")
                .holder("CARD HOLDER")
                .expirationDate("03/27")
                .build();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        redis.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Test
    @Transactional
    void addNewCard_success() {
        CardResponseDto createdCard = cardService.createCard(cardRequestDto, 1L);

        assertThat(createdCard.getId()).isNotNull();

        Optional<Card> card = cardDao.findById(createdCard.getId());
        assertThat(card.isPresent()).isTrue();
        CardResponseDto foundCard = cardMapper.toResponseDto(card.get());
        assertThat(foundCard.equals(createdCard)).isTrue();

        Cache cache = cacheManager.getCache("card:id");
        assertNotNull(cache);
        CardResponseDto cachedCard = cache.get(createdCard.getId(),
                CardResponseDto.class);
        assertEquals(cachedCard, foundCard);
    }

    @Test
    @Transactional
    void addNewCardToNotExistingUser_failure() {
        assertThrows(UserNotFoundException.class,
                () -> cardService.createCard(cardRequestDto, 11111L),
                "User with id 11111 does not exist");


    }

    @Test
    @Transactional
    void updateCard_withDuplicatedNumber_failure() {
        long id = cardService.createCard(cardRequestDto, 1L).getId();
        cardRequestDto.setNumber("0000111122223333");
        cardService.createCard(cardRequestDto, 1L);

        assertThrows(CardNumberNotUniqueException.class,
                () -> cardService.updateCard(cardRequestDto, id, 1L));
    }

    @Test
    @Transactional
    void newAddedCard_evictsUserCache_returnedWithNewUserServiceMethodCall_andCaches() {
        Cache userCache = cacheManager.getCache("user:id");
        assertNotNull(userCache);
        assertNotNull(userCache.get(userId, UserResponseDto.class));

        CardResponseDto createdCard = cardService.createCard(cardRequestDto, 1L);
        assertNull(userCache.get(userId, UserResponseDto.class));

        UserResponseDto userResponseDto = userService.getUserById(userId);
        List<CardResponseDto> cardResponseDtoList = userResponseDto.getCards();
        assertEquals(1, cardResponseDtoList.size());
        assertEquals(cardResponseDtoList.getFirst(), createdCard);

        UserResponseDto cachedUserWithCards =
                userCache.get(userId, UserResponseDto.class);
        assertNotNull(cachedUserWithCards);
        assertEquals(1, cachedUserWithCards.getCards().size());
        assertEquals(cardResponseDtoList.getFirst(),
                cachedUserWithCards.getCards().getFirst());
    }

    @Test
    @Transactional
    void deleteCard_evictsUserCache_notReturnedWithNewUserServiceMethodCall
            (@Autowired EntityManager entityManager) {
        CardResponseDto createdCard = cardService.createCard(cardRequestDto, 1L);

        cardService.deleteCard(createdCard.getId(), 1L);
        entityManager.flush();
        Cache userCache = cacheManager.getCache("user:id");
        if (userCache != null) {
            assertNull(userCache.get(userId, UserResponseDto.class));
        }

        UserResponseDto userResponseDto = userService.getUserById(userId);
        List<CardResponseDto> cardResponseDtoList = userResponseDto.getCards();
        assertTrue(cardResponseDtoList == null
                || cardResponseDtoList.isEmpty());
    }

    @Test
    @Transactional
    void deletingUser_deletesAllUserCards_andCaches() {
        CardResponseDto createdCard = cardService.createCard(cardRequestDto, 1L);

        userService.deleteUser(createdCard.getUserId());

        assertFalse(cardDao.findCardById(createdCard.getId()).isPresent());
        Cache cardCache = cacheManager.getCache("card:id");
        if (cardCache != null) {
            assertNull(cardCache.get(new SimpleKey(createdCard.getId(), createdCard.getUserId()), UserResponseDto.class));
        }
    }
}
