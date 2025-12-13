package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardGameLoanRepository extends JpaRepository<BoardGameLoan, Long> {
    List<BoardGameLoan> findAllByUserId(long id);

    Optional<BoardGameLoan> findFirstByitemsInLoan_BoardGame_NameAndStatus(String name, Status status);

    @Query("select b.itemsInLoan from BoardGameLoan b where b.id = :id")
    List<BoardGameItem> getBoardGameLoanById(@Param("id") long id);

    List<BoardGameLoan> findAllByStatus(Status status);

    @Query("SELECT COUNT(l) FROM BoardGameLoan l JOIN l.itemsInLoan i WHERE i.boardGame.id = :gameId")
    long countLoansByGameId(@Param("gameId") Long gameId);

    @Query("SELECT COUNT(l) FROM BoardGameLoan l JOIN l.itemsInLoan i WHERE i.id = :itemId")
    Long countLoansWithItem(@Param("itemId") Long itemId);
}
