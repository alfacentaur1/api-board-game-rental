package cz.cvut.fel.ear.service.interfaces;

import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.User;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanServiceI {

    /**
     * Finds a board game loan by its id
     * @param loanId id of the loan to find
     * @return the board game loan with the given id
     */
    BoardGameLoan getBoardGameLoan(long loanId);

    /**
     * Returns all board game loans in the system
     * @return list of all board game loans
     */
    List<BoardGameLoan> getLoans();

    /**
     * Returns all loans of a given user by user id
     * @param userId id of the user whose loans to find
     * @return list of board game loans for the given user
     */
    List<BoardGameLoan> getUserLoans(long userId);

    /**
     * Transactional <br>
     * Approves a board game loan by its id
     * @param loanId id of the loan to approve
     */
    @Transactional
    void approveLoan(long loanId);

    /**
     * Transactional <br>
     * Rejects a board game loan by its id
     * @param loanId id of the loan to reject
     */
    @Transactional
    void rejectLoan(long loanId);

    /**
     * Transactional <br>
     * Creates a new loan for a user with the given items and due date
     *
     * @param dueDate   date and time when the loan is due
     * @param gameNames list of board game names to borrow
     * @param user      user who borrows the items
     * @return id of the newly created loan
     */
    @Transactional
    long createLoan(LocalDateTime dueDate, List<String> gameNames, User user);

    /**
     * Transactional <br>
     * Marks a loan as returned
     * @param loan loan to mark as returned
     */
    @Transactional
    void returnLoan(BoardGameLoan loan);
}
