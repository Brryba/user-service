package user_service.exception.card;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class CardNotFoundException extends StatusCodeException {
    public CardNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Card with id " + id + " not found");
    }
}
