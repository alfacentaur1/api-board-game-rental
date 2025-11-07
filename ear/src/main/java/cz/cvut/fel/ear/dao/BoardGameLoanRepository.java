package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardGameLoanRepository extends JpaRepository<BoardGameLoan, Long> {

    /**
     * Finds all board game loans for a specific user
     * @param id user id
     * @return list of board game loans
     */
    List<BoardGameLoan> findAllByUserId(long id);

    /**
     * Finds the first available board game item by name and loan status
     * @param name name of the board game
     * @param loanStatus status of the loan
     * @return optional board game item
     */
    Optional<BoardGameLoan> findFirstByGamesToBeBorrowed_BoardGame_NameAndStatus(String name, LoanStatus loanStatus);

    /**
     * Gets all board games for a loan
     * @param id id of the loan
     * @return list of board game items
     */
    List<BoardGameItem> getBoardGameLoanById(long id);
}
