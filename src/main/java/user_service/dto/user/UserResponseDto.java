package user_service.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import user_service.dto.card.CardResponseDto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserResponseDto implements Serializable {
    private long id;
    private String name;
    private String surname;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String email;
    List<CardResponseDto> cards;
}
