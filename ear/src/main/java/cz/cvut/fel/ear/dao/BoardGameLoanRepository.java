package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardGameLoanRepository extends JpaRepository<BoardGameLoan, Long> {
    List<BoardGameLoan> findAllByUserId(long id);

    Optional<BoardGameLoan> findFirstByGamesToBeBorrowed_BoardGame_NameAndStatus(String name, Status status);

    List<BoardGameItem> getBoardGameLoanById(long id);
}
