package user_service.exception.user;

import org.springframework.http.HttpStatus;
import user_service.exception.StatusCodeException;

public class UsersNotFoundException extends StatusCodeException {
    public UsersNotFoundException() {
        super(HttpStatus.NOT_FOUND, "None of the users found");
    }
}
