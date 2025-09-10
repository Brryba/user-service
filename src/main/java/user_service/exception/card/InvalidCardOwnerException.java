package user_service.exception.card;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class InvalidCardOwnerException extends StatusCodeException {
    public InvalidCardOwnerException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
