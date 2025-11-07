package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {

    /**
     * Finds the ID of a board game by its name
     * @param gameName name of the board game
     * @return ID of the board game
     */
     int findBoardGameIdByGameName(String gameName);

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
    List<String> getAllBoardGameNames();

    /**
     * Finds a board game by its ID
     * @param id ID of the board game
     * @return board game
     */
    BoardGame findBoardGameById(long id);
}