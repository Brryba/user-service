package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CardNumberNotUniqueException extends ResponseStatusException {
    public CardNumberNotUniqueException() {
        super(HttpStatus.CONFLICT, "Card with the number already exists");
    }
}
