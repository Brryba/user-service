package user_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
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
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getUsersByIdsOrEmail(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) String email) {
        return new ResponseEntity<>(userService.getUsersByIdsOrEmail(ids, email), HttpStatus.OK);
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