package user_service.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user_service.dao.UserDao;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;
import user_service.exception.EmailAlreadyExistsException;
import user_service.exception.UserNotFoundException;
import user_service.exception.UsersNotFoundException;
import user_service.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserDao userDao;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    public UserService(UserDao userDao, UserMapper userMapper, CacheManager cacheManager) {
        this.userDao = userDao;
        this.userMapper = userMapper;
        this.cacheManager = cacheManager;
    }

    @Transactional
    @Caching(put = {
            @CachePut(value = "user:id", key = "#result.id"),
            @CachePut(value = "user:email", key = "#result.email")
    })
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        String email = userRequestDto.getEmail();
        if (userDao.findUserByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = userMapper.toUser(userRequestDto);
        userDao.save(user);
        return userMapper.toResponseDto(user);
    }

    @Cacheable("user:id")
    public UserResponseDto getUserById(Long id) {
        User user = userDao.findUserById(id).orElseThrow(() -> new UserNotFoundException(id));
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


    @Cacheable("user:email")
    public UserResponseDto getUserByEmail(String email) {
        User user = userDao.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toResponseDto(user);
    }

    @Transactional
    @Caching(put = {
            @CachePut(value = "user:id", key = "#id"),
            @CachePut(value = "user:email", key = "#result.email")
    })
    public UserResponseDto updateUser(UserRequestDto userRequestDto, long id) {
        User existingUser = userDao.findUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        String email = userRequestDto.getEmail();

        boolean emailChanged = !existingUser.getEmail().equals(email);
        if (emailChanged) {
            if (userDao.findUserByEmail(email).isPresent()) {
                throw new EmailAlreadyExistsException(email);
            }

            Cache emailCache = cacheManager.getCache("user:email");
            if (emailCache != null) {
                emailCache.evict(existingUser.getEmail());
            }
        }

        userMapper.updateUserFromDto(userRequestDto, existingUser);
        userDao.save(existingUser);
        return userMapper.toResponseDto(existingUser);
    }

    @Transactional
    @CacheEvict(value = "user:id", key = "#id")
    public void deleteUser(long id) {
        User user = userDao.findUserById(id).orElseThrow(() -> new UserNotFoundException(id));

        Cache emailCache = cacheManager.getCache("user:email");
        if (emailCache != null) {
            emailCache.evict(user.getEmail());
        }

        userDao.delete(user);
    }
}
