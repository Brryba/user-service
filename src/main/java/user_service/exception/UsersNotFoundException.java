package user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UsersNotFoundException extends ResponseStatusException {
    public UsersNotFoundException() {
        super(HttpStatus.NOT_FOUND, "None of the users found");
    }
}
