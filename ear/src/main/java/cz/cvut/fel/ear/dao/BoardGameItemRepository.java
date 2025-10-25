package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

//annotation for spring - persistence layer, managed by spring
@Repository
public interface BoardGameItemRepository extends JpaRepository<BoardGameItem, Long> {
    BoardGameItem getBoardGameItemById(long id);

    BoardGameItem findFirstByBoardGame_NameAndState(String boardGameName, BoardGameState state);
}
