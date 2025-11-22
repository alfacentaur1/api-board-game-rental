package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT g.name FROM RegisteredUser r JOIN r.favoriteBoardGames g WHERE r.id = :userId")
    List<String> findAllFavoriteGames(@Param("userId") long userId);

    User findByUsername(String username);
}
