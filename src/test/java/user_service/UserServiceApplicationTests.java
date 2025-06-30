package user_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import user_service.dto.card.IdCardRequestDto;
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
        IdCardRequestDto user = new IdCardRequestDto();
        user.setId(1L);
        user.setUserId(1L);
        user.setNumber("1111000022223333");
        user.setHolder("Smith Smith");
        user.setExpirationDate("12/24");

        Card responseDto = cardMapper.toCard(user);
        System.out.println(responseDto);
    }
}
