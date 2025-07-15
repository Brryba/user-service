package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CardNotFoundException extends ResponseStatusException {
    public CardNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Card with id " + id + " not found");
    }
}
