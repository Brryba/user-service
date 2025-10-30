package user_service.exception.camunda;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ParsingException extends RuntimeException {
    private String errorMessage;
}

