package user_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.exception.InvalidRequestException;
import user_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto addUser(@RequestBody @Valid UserRequestDto user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @GetMapping()
    public ResponseEntity<?> getUsersByIds(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) String email) {
        if (ids != null && email != null) {
            throw new InvalidRequestException("Specify user ids OR email");
        }
        if (email != null) {
            return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
        }
        if (ids != null) {
            return new ResponseEntity<>(userService.getUsersByIds(ids), HttpStatus.OK);
        }
        throw new InvalidRequestException("Specify user ids or email");
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateUser(@PathVariable long id,
                                                      @RequestBody @Valid UserRequestDto user) {
        return userService.updateUser(user, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }
}
