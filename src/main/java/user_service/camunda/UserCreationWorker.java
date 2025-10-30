package user_service.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.exception.BpmnError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.exception.camunda.ParsingException;
import user_service.exception.user.EmailAlreadyExistsException;
import user_service.service.UserService;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreationWorker {
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @JobWorker(type = "user_service_create")
    public Map<String, Object> handleUserCreation(@Variable String userRequest,
                                                  @Variable("user_id") Long userId) {
        UserRequestDto userDto = readUserRequest(userRequest);
        UserResponseDto userResponseDto = callUserService(userDto, userId);
        return createResponseMap(userResponseDto);
    }

    private UserRequestDto readUserRequest(String userRequest) {
        UserRequestDto userRequestDto;
        try {
            userRequestDto = objectMapper.readValue(userRequest, UserRequestDto.class);
            log.info("Received userRequest: email={}", userRequestDto.getEmail());
        } catch (JsonProcessingException e) {
            log.error("Error parsing userRequest:", e);
            throw new ParsingException("Failed to serialize userResponse");
        }
        return userRequestDto;
    }

    private UserResponseDto callUserService(UserRequestDto userRequest, Long userId) {
        try {
            UserResponseDto userResponse = userService.createUser(userRequest, userId);
            log.info("Created user with id {}", userResponse.getId());
            return userResponse;
        } catch (EmailAlreadyExistsException e) {
            log.warn("Email duplicate exception. Email {} is not unique. Bpmn USER_SERVICE_ERROR was thrown", userRequest.getEmail());
            throw new BpmnError(
                    "USER_SERVICE_ERROR",
                    e.getMessage(),
                    Map.of("error", e.getMessage()),
                    e
            );
        }
    }

    private Map<String, Object> createResponseMap(UserResponseDto userResponseDto) {
        String userResponse;
        try {
            userResponse = objectMapper.writeValueAsString(userResponseDto);
        } catch (JsonProcessingException e) {
            log.error("Error parsing userResponse:", e);
            throw new ParsingException("Failed to serialize userResponse");
        }
        log.info("Completed task by creating user with {} email", userResponseDto.getEmail());
        return Map.of("userResponse", userResponse);
    }
}

