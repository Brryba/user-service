package user_service.dto.user;

import lombok.Builder;
import lombok.Data;
import user_service.dto.card.CardResponseDto;
import user_service.entity.Card;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UserResponseDto implements Serializable {
    private long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    List<CardResponseDto> cards;
}
