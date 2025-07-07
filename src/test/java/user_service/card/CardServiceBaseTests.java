package user_service.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user_service.dao.CardDao;
import user_service.dao.UserDao;
import user_service.dto.card.CardRequestDto;
import user_service.entity.Card;
import user_service.entity.User;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class CardServiceBaseTests {
    @MockitoBean
    protected CardDao cardDao;

    @MockitoBean
    protected UserDao userDao;

    protected Card card;

    protected CardRequestDto cardRequestDto;

    protected User cardUser;

    @BeforeEach
    protected void initializeCard() {
        cardUser = buildUser();
        card = buildCard();
        cardRequestDto = buildCardRequestDto();
    }

    protected Card buildCard() {
        return Card.builder()
                .id(1L)
                .user(this.cardUser)
                .holder("Some Holder")
                .expirationDate("10/27")
                .number("0011223344556677")
                .build();
    }

    protected CardRequestDto buildCardRequestDto() {
        return CardRequestDto.builder()
                .userId(1L)
                .holder("Some Holder")
                .expirationDate("10/27")
                .number("0011223344556677")
                .build();
    }

    protected User buildUser() {
        return User.builder()
                .id(1L)
                .name("Name")
                .surname("Surname")
                .email("email@email.com")
                .birthDate(LocalDate.now().minusYears(20))
                .build();
    }
}
