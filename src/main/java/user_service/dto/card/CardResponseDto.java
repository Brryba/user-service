package user_service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardResponseDto {
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private String expirationDate;
}
