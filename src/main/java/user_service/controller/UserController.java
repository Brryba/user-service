package user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal Long userId) {
        return userService.getUserById(userId);
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

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createCurrentUserProfile(@Valid @RequestBody UserRequestDto userRequestDto,
                                                    @PathVariable Long userId) {
        return userService.createUser(userRequestDto, userId);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateCurrentUser(@RequestBody @Valid UserRequestDto user,
                                             @AuthenticationPrincipal Long userId) {
        return userService.updateUser(user, userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@AuthenticationPrincipal Long userId) {
        userService.deleteUser(userId);
    }
}