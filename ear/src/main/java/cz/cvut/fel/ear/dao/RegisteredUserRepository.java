package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {

    List<BoardGame> findAllFavoriteGames(long id);

    /**
     * Finds a registered user by its ID
     * @param id ID of the registered user
     * @return registered user
     */
    RegisteredUser findRegisteredUserById(long id);
}
