package user_service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user_service.dao.UserDao;
import user_service.dto.user.UserRequestDto;
import user_service.entity.Card;
import user_service.entity.User;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UserServiceBaseTests {
    @MockitoBean
    protected UserDao userDao;

    protected User user;

    protected UserRequestDto userRequestDto;

    @BeforeEach
    public void initializeUser() {
        user = buildUser();
        userRequestDto = buildUserRequestDto();
        setUserCards();
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

    private void setUserCards() {
        user.setCards(List.of(Card.builder().id(1L).user(user).build(),
                Card.builder().id(2L).user(user).build()));
    }

    private UserRequestDto buildUserRequestDto() {
        return UserRequestDto.builder()
                .name("Name")
                .surname("Surname")
                .email("email@email.com")
                .birthDate(LocalDate.now().minusYears(20))
                .build();
    }
}
