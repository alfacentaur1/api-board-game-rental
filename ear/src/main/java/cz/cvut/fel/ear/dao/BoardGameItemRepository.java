package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import cz.cvut.fel.ear.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//annotation for spring - persistence layer, managed by spring
@Repository
public interface BoardGameItemRepository extends JpaRepository<BoardGameItem, Long> {
    BoardGameItem getBoardGameItemById(long id);

    List<BoardGameItem> findAvailableByNameWithLock(
            @Param("name") String name,
            @Param("state") BoardGameState state
    );

}
