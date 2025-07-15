package user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "card_info")
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, unique = true, length = 16)
    private String number;
    @Column(nullable = false)
    private String holder;
    @Column(nullable = false, length = 5)
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$")
    private String expirationDate;
}
