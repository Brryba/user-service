package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserProfileAlreadyExistsException extends ResponseStatusException {
    public UserProfileAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
