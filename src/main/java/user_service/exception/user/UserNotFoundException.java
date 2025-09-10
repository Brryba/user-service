package user_service.exception.user;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class UserNotFoundException extends StatusCodeException {
    public UserNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "User with id " + id + " not found. Create account first");
    }

    public UserNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "User with email " + email + " not found");
    }
}
