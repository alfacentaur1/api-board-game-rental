package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {
List<String> findAllFavoriteGames(long id);
RegisteredUser findRegisteredUserById(long id);
}
