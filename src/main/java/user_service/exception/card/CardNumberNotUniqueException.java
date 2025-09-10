package user_service.exception.card;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class CardNumberNotUniqueException extends StatusCodeException {
    public CardNumberNotUniqueException() {
        super(HttpStatus.CONFLICT, "Card with the number already exists");
    }
}
