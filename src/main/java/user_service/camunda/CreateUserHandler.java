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
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Component;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.service.UserService;
import user_service.exception.camunda.BpmnException;
import user_service.exception.camunda.ParsingException;

import java.util.Map;

@Component
@Slf4j
@ExternalTaskSubscription("user_service_create")
@RequiredArgsConstructor
public class CreateUserHandler implements ExternalTaskHandler {

    private static final String SERVICE_ERROR = "USER_SERVICE_ERROR";
    private static final String USER_REQUEST_VAR = "userRequest";
    private static final String USER_RESPONSE_VAR = "userResponse";
    private static final String USER_ID_VAR = "user_id";

    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            Long userId = externalTask.getVariable(USER_ID_VAR);
            UserRequestDto userRequestDto = readUserRequest(externalTask);
            UserResponseDto userResponseDto = createUser(userRequestDto, userId);
            completeTask(externalTask, externalTaskService, userResponseDto);

        } catch (BpmnException e) {
            log.error("BPMN error: {}", e.getMessage());
            externalTaskService.setVariables(externalTask, Map.of("error", e.getMessage()));
            externalTaskService.handleBpmnError(externalTask, SERVICE_ERROR, e.getMessage());

        } catch (ParsingException e) {
            log.error("Parsing failure: {}", e.getMessage(), e);
            externalTaskService.handleFailure(externalTask, "Error parsing JSON",
                    e.getErrorMessage(), 3, 5000);
        }
    }

    private UserRequestDto readUserRequest(ExternalTask externalTask) {
        JsonValue jsonValue = externalTask.getVariableTyped(USER_REQUEST_VAR);
        if (jsonValue == null) {
            throw new ParsingException("userRequest variable is missing");
        }
        try {
            UserRequestDto userRequest = objectMapper.readValue(jsonValue.getValue(), UserRequestDto.class);
            log.info("Received userRequest DTO: {}", userRequest);
            return userRequest;
        } catch (JsonProcessingException e) {
            throw new ParsingException("Failed to parse userRequest");
        }
    }

    private UserResponseDto createUser(UserRequestDto userRequestDto, Long userId) {
        try {
            UserResponseDto userResponse = userService.createUser(userRequestDto, userId);
            log.info("Created user with ID: {}", userResponse.getId());
            return userResponse;
        } catch (Exception e) {
            throw new BpmnException(e.getMessage());
        }
    }

    private void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService,
                              UserResponseDto userResponseDto) {
        VariableMap variables = Variables.createVariables();
        variables.put(USER_ID_VAR, userResponseDto.getId());
        try {
            variables.put(USER_RESPONSE_VAR, objectMapper.writeValueAsString(userResponseDto));
        } catch (JsonProcessingException e) {
            throw new ParsingException("Failed to serialize userResponse");
        }
        externalTaskService.complete(externalTask, variables);
        log.info("Completed task for user ID: {}", userResponseDto.getId());
    }
}
