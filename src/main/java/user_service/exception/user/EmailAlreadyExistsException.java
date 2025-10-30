package user_service.exception.user;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class EmailAlreadyExistsException extends StatusCodeException {
    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "Email address " + email +" already exists");
    }
}
