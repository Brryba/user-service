package user_service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;
import user_service.exception.EmailAlreadyExistsException;
import user_service.exception.UserNotFoundException;
import user_service.exception.UsersNotFoundException;
import user_service.mapper.CardMapperImpl;
import user_service.mapper.UserMapperImpl;
import user_service.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {UserService.class,
        UserMapperImpl.class,
        CardMapperImpl.class})
public class UserServiceLayerUnitTests extends UserServiceBaseTests {
    @Autowired
    private UserService userService;

    @MockitoBean
    private CacheManager cacheManager;

    private boolean requestResponseEquals(UserRequestDto request,
                                          UserResponseDto response) {
        return request.getName().equals(response.getName()) &&
                request.getSurname().equals(response.getSurname()) &&
                request.getEmail().equals(response.getEmail()) &&
                request.getBirthDate().equals(response.getBirthDate());
    }

    @Test
    public void createCardTest_success() {
        given(userDao.save(any(User.class))).willReturn(user);

        UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getId()).isNotNull();
        assertThat(requestResponseEquals(userRequestDto, userResponseDto)).isTrue();
    }

    @Test
    public void createCardTest_failure_emailAlreadyExists() {
        given(userDao.save(any(User.class))).willReturn(user);
        given(userDao.findUserByEmail("email@email.com")).willReturn(Optional.of(user));

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(userRequestDto),
                "Email address email@email.com already exists");
    }

    @Test
    public void getUserByIdTest_success() {
        given(userDao.findUserById(1L)).willReturn(Optional.ofNullable(user));

        UserResponseDto userResponseDto = userService.getUserById(1L);

        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getId()).isEqualTo(1L);
    }

    @Test
    public void getUserByIdTest_throwsException_whenUserIdDoesNotExist() {
        given(userDao.findUserById(1L)).willReturn(Optional.ofNullable(user));

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L),
                "User with id 2 not found");
    }

    @Test
    public void getUserByEmailTest_success() {
        given(userDao.findUserByEmail("email@email.com"))
                .willReturn(Optional.ofNullable(user));

        UserResponseDto userResponseDto = userService.getUserByEmail("email@email.com");

        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getId()).isEqualTo(1L);
    }

    @Test
    public void getUserByEmailTest_throwsException_whenEmailDoesNotExist() {
        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("email@email.com"),
                "User with email email@email.com not found");
    }

    @Test
    public void getAllUsersByIdsTest_success() {
        User user2 = buildUser();
        user2.setId(2L);
        given(userDao.findUsersByIdIn(List.of(1L, 2L))).willReturn(List.of(user, user2));

        List<UserResponseDto> users = userService.getUsersByIds(List.of(1L, 2L));

        assertThat(users).hasSize(2);
        assertThat(users.getFirst().getId()).isEqualTo(1L);
        assertThat(users.get(1).getId()).isEqualTo(2L);
    }

    @Test
    public void getAllUsersByIdsTest_notAllUsers() {
        given(userDao.findUsersByIdIn(List.of(1L, 2L))).willReturn(List.of(user));

        List<UserResponseDto> users = userService.getUsersByIds(List.of(1L, 2L));

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    public void getAllUsersByIdsTest_noUsersFound_throwsException() {
        assertThrows(UsersNotFoundException.class,
                () -> userService.getUsersByIds(List.of(12345L, 67890L)));
    }

    @Test
    public void updateUserTest_success() {
        given(userDao.save(any(User.class))).willReturn(user);
        given(userDao.findUserById(1L)).willReturn(Optional.ofNullable(user));

        userService.createUser(userRequestDto);
        user.setName("Updated");
        userRequestDto.setName("Updated");

        UserResponseDto userResponseDto = userService.updateUser(userRequestDto, 1L);
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getId()).isEqualTo(1L);
        assertThat(requestResponseEquals(userRequestDto, userResponseDto)).isTrue();
    }

    @Test
    public void updateUserTest_userNotFound_throwsException() {
        given(userDao.findUserById(1L)).willReturn(Optional.ofNullable(user));

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(userRequestDto, 2L));
    }

    @Test
    public void updateUserTest_failure_emailAlreadyExists() {
        User user2 = buildUser();
        user2.setId(2L);
        user2.setEmail("another@email.com");
        userRequestDto.setEmail("email@email.com");

        given(userDao.save(any(User.class))).willReturn(user2);
        given(userDao.findUserById(2L)).willReturn(Optional.of(user2));
        given(userDao.findUserByEmail("email@email.com"))
                .willReturn(Optional.ofNullable(user));

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateUser(userRequestDto, 2L));
    }

    @Test
    public void deleteUserTest_success() {
        when(userDao.findUserById(1L)).thenReturn(Optional.ofNullable(user));
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("card:id")).thenReturn(mockCache);
        doNothing().when(mockCache).evict(any(Long.class));
        willDoNothing().given(userDao).delete(user);

        userService.deleteUser(1L);

        verify(userDao, times(1)).delete(user);
    }

    @Test
    public void deleteUserTest_throwsException_whenUserDoesNotExist() {
        given(userDao.findUserById(1L)).willReturn(Optional.ofNullable(user));

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(2L));
    }
}
