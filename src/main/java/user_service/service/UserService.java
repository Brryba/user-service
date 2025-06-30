package user_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user_service.dao.UserDao;
import user_service.dto.user.*;
import user_service.entity.User;
import user_service.exception.*;
import user_service.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserDao userDao;
    private final UserMapper userMapper;

    public UserService(UserDao userDao, UserMapper userMapper) {
        this.userDao = userDao;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        String email = userRequestDto.getEmail();
        if (userDao.findUserByEmail(email) != null) {
            throw new EmailAlreadyExistsException();
        }

        User user = userMapper.toUser(userRequestDto);
        userDao.save(user);
        return userMapper.toResponseDto(user);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userDao.findUserById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return userMapper.toResponseDto(user);
    }

    public List<UserResponseDto> getUsersByIds(List<Long> ids) {
        List<User> users = userDao.findUsersByIdIn(ids);
        if (users == null || users.isEmpty()) {
            throw new UsersNotFoundException();
        }
        return users
                .stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = userDao.findUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(email);
        }
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(UserRequestDto userRequestDto, long id) {
        User existingUser = userDao.findUserById(id);
        if (existingUser == null) {
            throw new UserNotFoundException(id);
        }
        String email = userRequestDto.getEmail();
        if (!existingUser.getEmail().equals(email)
                && userDao.findUserByEmail(email) != null) {
            throw new EmailAlreadyExistsException();
        }

        User updatedUser = userMapper.toUser(userRequestDto);
        updatedUser.setId(id);
        userDao.save(updatedUser);
        return userMapper.toResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(long id) {
        User user = userDao.findUserById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }

        userDao.delete(user);
    }
}
