package user_service.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import user_service.entity.Card;

@Repository
public interface CardDao extends JpaRepository<Card, Long> {
}
