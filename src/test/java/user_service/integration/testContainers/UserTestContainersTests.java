package user_service.integration.testContainers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import user_service.dao.UserDao;
import user_service.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
@Testcontainers
public class UserTestContainersTests {
    @Autowired
    private UserDao userDao;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );

    private static User userEntity;

    @BeforeAll
    static void beforeAll(@Autowired UserDao userDao) {
        postgres.start();

        userEntity = User.builder()
                .name("test")
                .surname("test")
                .email("test@test.com")
                .birthDate(LocalDate.now().minusYears(1))
                .cards(new ArrayList<>())
                .build();
        userEntity = userDao.save(userEntity);
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void findUserById_success() {
        Optional<User> foundUser = userDao.findById(userEntity.getId());

        assertThat(foundUser.isPresent()).isTrue();
        assertThat(foundUser.get().getCards()).hasSameElementsAs(userEntity.getCards());
        assertThat(foundUser.get()).isEqualTo(userEntity);
    }

    @Test
    void findUserById_failure_userNotFound() {
        Optional<User> foundUser = userDao.findById(1000L);
        assertThat(foundUser).isEmpty();
    }
}
