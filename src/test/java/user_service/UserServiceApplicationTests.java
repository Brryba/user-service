package user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import user_service.dao.UserDao;

import java.util.List;

@SpringBootTest
class UserServiceApplicationTests {
    public UserServiceApplicationTests(UserDao userDao) {
        this.userDao = userDao;
    }

    private final UserDao userDao;

    @Test
    void contextLoads() {
    }

}
