package user_service.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDto {
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
    @Email(message = "Incorrect email format")
    protected String email;
}