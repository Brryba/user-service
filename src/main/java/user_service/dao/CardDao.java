package user_service.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import user_service.entity.Card;

import java.util.List;

@Repository
public interface CardDao extends JpaRepository<Card, Long> {
    @Query("SELECT card FROM Card card WHERE card.id = :id")
    public Card findCardById(Long id);

    public List<Card> findCardsByIdIn(List<Long> ids);

    @Modifying
    @Query(value = "DELETE FROM public.card_info " +
            "WHERE card_info.id = :id", nativeQuery = true)
    public void deleteCardById(Long id);
}
