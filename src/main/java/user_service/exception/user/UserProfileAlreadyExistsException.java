package user_service.exception.user;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class UserProfileAlreadyExistsException extends StatusCodeException {
    public UserProfileAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
