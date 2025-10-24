package user_service.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.springframework.stereotype.Component;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.service.UserService;

@Component
@Slf4j
@ExternalTaskSubscription("user_service_create")
@RequiredArgsConstructor
public class CreateUserHandler implements ExternalTaskHandler {
    private final String SERVICE_ERROR = "USER_SERVICE_ERROR";
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Long userId = externalTask.getVariable("user_id");
        log.info("Received create user external task for {} user", userId);

        JsonValue jsonValue = externalTask.getVariableTyped("userRequest");
        if (jsonValue == null) {
            log.error("userRequest variable is null!");
            externalTaskService.handleBpmnError(externalTask, SERVICE_ERROR, "The userRequest " +
                    "variable was not provided");
            return;
        }

        String jsonString = jsonValue.getValue();

        UserRequestDto userRequestDto;
        try {
            userRequestDto = objectMapper.readValue(jsonString, UserRequestDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing userRequest", e);
            externalTaskService.handleFailure(externalTask, "Error parsing userRequest", e.getMessage(), 0, 0);
            return;
        }

        log.info("created user dto: {} with {} id", userRequestDto, userId);
        UserResponseDto userResponseDto;
        try {
            userResponseDto = userService.createUser(userRequestDto, userId);
            log.info("created user with ID: {}", userResponseDto.getId());
        } catch (Exception e) {
            externalTaskService.handleBpmnError(externalTask, SERVICE_ERROR, e.getMessage());
            log.error("Error creating user: " + e.getMessage());
            return;
        }

        externalTaskService.complete(externalTask);
    }
}