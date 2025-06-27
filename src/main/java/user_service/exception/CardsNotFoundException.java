package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class CardsNotFoundException extends ResponseStatusException {

    public CardsNotFoundException() {
        super(HttpStatus.NOT_FOUND, "None of the cards were found");
    }
}
