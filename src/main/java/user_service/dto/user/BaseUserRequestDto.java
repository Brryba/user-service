package user_service.dto.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseUserRequestDto {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not be longer than 100 letters")
    protected String name;
    @NotBlank(message = "Surname is required")
    @Size(max = 100, message = "Surname must not be longer than 100 letters")
    protected String surname;
    @Past(message = "Birth date must be in the past")
    protected LocalDate birthDate;
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must be shorter than 100 symbols")
    protected String email;
}