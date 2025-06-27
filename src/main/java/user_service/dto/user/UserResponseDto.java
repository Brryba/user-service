package user_service.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponseDto {
    private long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
