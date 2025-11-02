package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {

    BoardGame getBoardGameById(long id);

    @Query("SELECT b.name FROM BoardGame b")
    List<String> getAllBoardGameNames();

    BoardGame findBoardGameById(long id);
}