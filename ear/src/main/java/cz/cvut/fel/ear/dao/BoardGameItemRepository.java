package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

//annotation for spring - persistence layer, managed by spring
@Repository
public interface BoardGameItemRepository extends JpaRepository<BoardGameItem, Long> {

    /**
     * Finds a board game item by the name of the board game
     * @param boardGameName name of the board game
     * @return found board game item
     */
    BoardGameItem findBoardGameItemByBoardGame_Name(String boardGameName);

    /**
     * Gets a board game item by its id
     * @param id id of the board game item
     * @return board game item
     */
    BoardGameItem getBoardGameItemById(long id);

    /**
     * Finds the first board game item by the name of the board game and its state
     * @param name name of the board game
     * @param state state of the board game item
     * @return board game item
     */
    List<BoardGameItem> findAvailableByNameWithLock(
            @Param("name") String name,
            @Param("state") BoardGameState state
    );
}
