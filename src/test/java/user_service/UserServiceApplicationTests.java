package user_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import user_service.entity.Card;
import user_service.mapper.CardMapper;
import user_service.mapper.UserMapper;

@SpringBootTest
class UserServiceApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CardMapper cardMapper;

    @Test
    void contextLoads() {
    }

    @Test
    public void testMapper() {
    }
}
