package user_service.integration.testContainers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import user_service.dao.UserDao;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;
import user_service.exception.EmailAlreadyExistsException;
import user_service.mapper.UserMapper;
import user_service.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@EnableCaching
@Testcontainers
public class UserTestContainersTests {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserMapper userMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );


    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
                    .withExposedPorts(6379);

    private static UserRequestDto userRequestDto;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        redis.start();
    }

    @BeforeEach
    public void beforeEach() {
        userRequestDto = UserRequestDto.builder()
                .name("test")
                .surname("test")
                .email("test@test.com")
                .birthDate(LocalDate.now().minusYears(1))
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
    void testRedisConnection() {
        assertThat(redis.isRunning()).isTrue();
    }

    @Test
    @Transactional
    void addNewUserTest_success() {
        UserResponseDto userResponseDto = userService.createUser(userRequestDto, 1L);

        Optional<User> foundUser = userDao.findUserById(userResponseDto.getId());
        assertThat(foundUser.isPresent()).isTrue();
        User user = foundUser.get();
        assertThat(user.getId()).isEqualTo(userResponseDto.getId());
        assertThat(user.getName()).isEqualTo(userRequestDto.getName());

        assertThat(cacheManager.getCache("user:id")).isNotNull();
        assertThat(cacheManager.getCache("user:id")).isInstanceOf(Cache.class);
        Cache cache = cacheManager.getCache("user:id");

        UserResponseDto cachedResponse = cache.get(userResponseDto.getId(), UserResponseDto.class);
        assertThat(cachedResponse).isEqualTo(userResponseDto);
    }

    @Test
    @Transactional
    void addNewUserTest_duplicateEmail() {
        UserResponseDto firstResponseDto = userService.createUser(userRequestDto, 1L);
        String previousName = userRequestDto.getName();

        userRequestDto.setName("New Name");
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(userRequestDto, 1L));

        UserResponseDto foundUser = userService.getUserById(firstResponseDto.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo(previousName);
        assertThat(foundUser.getName()).isNotEqualTo(userRequestDto.getName());

        assertThat(cacheManager.getCache("user:id")).isNotNull();
        Cache cache = cacheManager.getCache("user:id");
        assertThat(cache).isNotNull();
        UserResponseDto cachedResponse = cache.get(firstResponseDto.getId(), UserResponseDto.class);
        assertThat(cachedResponse).isEqualTo(firstResponseDto);

        userRequestDto.setName(previousName);
    }

    @Test
    @Transactional
    void getUsersByIdsTest_success() {
        UserResponseDto first = userService.createUser(userRequestDto, 1L);
        userRequestDto.setEmail("anotheremail@test.com");
        UserResponseDto second = userService.createUser(userRequestDto, 2L);

        System.out.println(List.of(first.getId(), second.getId()));
        List<UserResponseDto> users = userService.getUsersByIds(List.of(first.getId(),
                second.getId()));
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    @Transactional
    void getUsersByEmailTest_success() {
        UserResponseDto savedUserDto = userService.createUser(userRequestDto, 1L);

        UserResponseDto foundUserDto = userMapper.toResponseDto(
                userDao.findUserByEmail(savedUserDto.getEmail()).orElseThrow());
        assertThat(foundUserDto).isNotNull();
        assertThat(foundUserDto).isEqualTo(savedUserDto);

        UserResponseDto cachedUserDto = cacheManager.getCache("user:id").
                get(savedUserDto.getId(), UserResponseDto.class);
        assertThat(cachedUserDto).isEqualTo(savedUserDto);
        assertThat(cachedUserDto).isEqualTo(foundUserDto);
    }

    @Test
    @Transactional
    void updateUserTest_success() {
        UserResponseDto createdUser = userService.createUser(userRequestDto, 1L);

        userRequestDto.setEmail("anotheremail@test.com");
        userRequestDto.setName("New Name");
        userRequestDto.setSurname("New Surname");

        UserResponseDto updatedUserDto = userService.updateUser(userRequestDto, createdUser.getId());
        UserResponseDto dbUser = userMapper.toResponseDto(
                userDao.findUserById(createdUser.getId()).orElseThrow()
        );
        assertThat(dbUser).isNotNull();
        assertThat(dbUser).isEqualTo(updatedUserDto);
        assertThat(dbUser).isNotEqualTo(createdUser);

        Cache cache = cacheManager.getCache("user:id");
        assertThat(cache).isNotNull();
        UserResponseDto cachedResponse = cache.get(dbUser.getId(), UserResponseDto.class);
        assertThat(cachedResponse).isEqualTo(updatedUserDto);
        assertThat(cachedResponse).isNotEqualTo(createdUser);
    }

    @Test
    @Transactional
    void deleteUserTest_success() {
        UserResponseDto createdUser = userService.createUser(userRequestDto, 1L);
        userService.deleteUser(createdUser.getId());

        assertThat(userDao.findUserById(createdUser.getId()).isPresent()).isFalse();

        Cache cache = cacheManager.getCache("user:id");
        if (cache != null) {
            UserResponseDto cachedResponse = cache.get(createdUser.getId(), UserResponseDto.class);
            assertThat(cachedResponse).isNull();
        }
    }
}
