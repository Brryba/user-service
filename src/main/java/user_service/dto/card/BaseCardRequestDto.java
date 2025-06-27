package user_service.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCardRequestDto {
    @NotNull(message = "User id is missing")
    protected Long userId;
    @NotBlank(message = "Card number is missing")
    @Size(min = 16, max = 16,
            message = "Card number should consist of 16 digits")
    @Pattern(regexp = "^[0-9]{16}$",
            message = "Card number should only contains digits")
    protected String number;
    @NotBlank(message = "Holder is missing")
    protected String holder;
    @NotBlank(message = "Expiration date is missing")
    @Pattern(regexp = "^(0[1-9]|1[0-2]/[0-9]{2})$",
            message = "Expiration date should be in MM/YY format")
    protected String expirationDate;
}
