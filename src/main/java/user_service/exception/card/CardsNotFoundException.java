package user_service.exception.card;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class CardsNotFoundException extends StatusCodeException {

    public CardsNotFoundException() {
        super(HttpStatus.NOT_FOUND, "None of the cards were found");
    }
}
