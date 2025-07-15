package user_service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponseDto implements Serializable {
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private String expirationDate;
}
