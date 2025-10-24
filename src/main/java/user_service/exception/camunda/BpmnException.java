package user_service.exception.camunda;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BpmnException extends RuntimeException {
    private String message;
}
