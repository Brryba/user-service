package user_service.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRequestDto {
    @NotNull(message = "User id is missing")
    private Long userId;
    @NotBlank(message = "Card number is missing")
    @Pattern(regexp = "^[0-9]{16}$",
            message = "Card number should only contains 16 digits")
    private String number;
    @NotBlank(message = "Holder is missing")
    private String holder;
    @NotBlank(message = "Expiration date is missing")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$",
            message = "Expiration date should be in MM/YY format")
    private String expirationDate;
}
