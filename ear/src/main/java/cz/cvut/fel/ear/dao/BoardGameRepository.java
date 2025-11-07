package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {

    /**
     * Gets a board game by its ID
     * @param id ID of the board game
     * @return board game
     */
    BoardGame getBoardGameById(long id);

    /**
     * Gets all board game names
     * @return list of board game names
     */
    @Query("SELECT b.name FROM BoardGame b")
    List<String> getAllBoardGameNames();

}