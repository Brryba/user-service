package user_service.dao;

import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import user_service.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
@Table(name = "users")
public interface UserDao extends CrudRepository<User, Long> {
    public Optional<User> findUserById(Long id);

    @EntityGraph(attributePaths = {"cards"})
    @Query("SELECT user FROM User user " +
            "WHERE user.id IN :ids")
    List<User> findUsersByIdIn(List<Long> ids);

    public Optional<User> findUserByEmail(String email);

    public void deleteUserById(Long id);
}
