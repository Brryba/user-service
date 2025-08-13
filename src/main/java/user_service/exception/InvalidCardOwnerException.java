package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidCardOwnerException extends ResponseStatusException {
    public InvalidCardOwnerException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
