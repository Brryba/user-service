package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {
    public UserNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "User with id " + id + " not found");
    }

    public UserNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "User with email " + email + " not found");
    }
}
