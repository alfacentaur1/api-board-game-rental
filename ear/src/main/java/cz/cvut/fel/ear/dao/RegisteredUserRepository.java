package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {

    @Query("SELECT g.name FROM RegisteredUser r JOIN r.favoriteBoardGames g WHERE r.id = :userId")
    List<String> findAllFavoriteGames(@Param("userId") long userId);

    RegisteredUser findByUsername(String username);
}
