package user_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "card_info")
@Data
public class Card {
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String number;
    private String holder;
    private LocalDate expirationDate;
}
