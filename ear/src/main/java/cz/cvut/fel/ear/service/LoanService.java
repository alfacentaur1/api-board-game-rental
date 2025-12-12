package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.exception.*;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanService {

    private final BoardGameLoanRepository boardGameLoanRepository;
    private final BoardGameItemRepository boardGameItemRepository;
    private final UserService userService;

    public LoanService(BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository, @Lazy UserService userService) {
        this.boardGameLoanRepository = boardGameLoanRepository;
        this.boardGameItemRepository = boardGameItemRepository;
        this.userService = userService;
    }

    /**
     * Retrieves a loan by its ID.
     *
     * @param loanId the ID of the loan
     * @return the BoardGameLoan entity
     * @throws EntityNotFoundException if the loan is not found
     */
    public BoardGameLoan getBoardGameLoan(long loanId) {
        return boardGameLoanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException(BoardGameLoan.class.getSimpleName(), loanId));
    }

    /**
     * Retrieves all loans stored in the database.
     *
     * @return a list of all BoardGameLoan entities
     */
    public List<BoardGameLoan> getBoardGameLoans() {
        return boardGameLoanRepository.findAll();
    }

    /**
     * Retrieves all loans associated with a specific user.
     *
     * @param userId the ID of the user
     * @return a list of the user's loans
     * @throws EntityNotFoundException if the user has no loans or user does not exist
     */
    public List<BoardGameLoan> getAllBoardGameLoansByUser(long userId) {
        List<BoardGameLoan> loans = boardGameLoanRepository.findAllByUserId(userId);
        if (loans == null) {
            throw new EntityNotFoundException(BoardGameLoan.class.getSimpleName(), null, User.class.getSimpleName(), userId);
        }
        return loans;
    }

    /**
     * Approves a loan request, changing its status to APPROVED.
     *
     * @param loanId the ID of the loan to approve
     */
    public void approveGameLoan(long loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        boardGameLoan.setStatus(Status.approved);
    }

    /**
     * Rejects a loan request.
     * Sets status to REJECTED and returns the items to the loanable state.
     *
     * @param loanId the ID of the loan to reject
     */
    public void rejectGameLoan(long loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        List<BoardGameItem> loanBoardGameItems = boardGameLoan.getItems();
        for (BoardGameItem boardGameItem : loanBoardGameItems) {
            boardGameItem.setState(BoardGameState.FOR_LOAN);
        }
        boardGameLoan.setStatus(Status.rejected);
    }

    /**
     * Manually changes the status of a loan.
     *
     * @param loanId    the ID of the loan
     * @param newStatus the new status to set
     * @throws InvalidStatusException if the status is null or invalid
     */
    public void changeLoanStatus(long loanId, Status newStatus) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        if (newStatus == null) {
            throw new InvalidStatusException("Invalid status null");
        }
        try {
            Status.valueOf(newStatus.name());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid status " + newStatus.name());
        }
        boardGameLoan.setStatus(newStatus);
    }

    /**
     * Retrieves a list of all board game items that are currently marked as BORROWED.
     *
     * @return a list of borrowed BoardGameItem entities
     */
    public List<BoardGameItem> currentlyBorrowedBoardGameItems() {
        List<BoardGameItem> boardGameItems = boardGameItemRepository.findAll();
        List<BoardGameItem> currentlyBorrowedBoardGameItems = new ArrayList<>();
        for (BoardGameItem boardGameItem : boardGameItems) {
            if (boardGameItem.getState().equals(BoardGameState.BORROWED)) {
                currentlyBorrowedBoardGameItems.add(boardGameItem);
            }
        }
        return currentlyBorrowedBoardGameItems;
    }

    /**
     * Creates a new loan transaction.
     * Checks availability of games, locks items, and assigns them to the user.
     *
     * @param dueDate        the date by which the games should be returned
     * @param boardGameNames list of names of games to borrow
     * @param userId         the ID of the user borrowing the games
     * @return the ID of the newly created loan
     * @throws InvalidDateException         if due date is in the past
     * @throws ParametersException          if game list is empty
     * @throws NotAvalaibleInStockException if any game is unavailable
     */
    @Transactional
    public long createLoan(LocalDateTime dueDate, List<String> boardGameNames, long userId) {
        LocalDateTime now = LocalDateTime.now();

        if (dueDate.isBefore(now)) {
            throw new InvalidDateException("Due date is before current date");
        }

        if (boardGameNames == null || boardGameNames.isEmpty()) {
            throw new ParametersException("There are no board games to borrow (boardGameNames is empty");
        }

        BoardGameLoan newLoan = new BoardGameLoan();
        List<BoardGameItem> itemsToBorrow = new ArrayList<>();

        for (String name : boardGameNames) {
            List<BoardGameItem> allAvailableItems = boardGameItemRepository.findAvailableByNameWithLock(name, BoardGameState.FOR_LOAN);

            if (allAvailableItems.isEmpty()) {
                throw new NotAvalaibleInStockException(
                        String.format("Board game %s has no available items to borrow", name)
                );
            }

            BoardGameItem itemToBorrow = allAvailableItems.getFirst();

            itemToBorrow.setState(BoardGameState.BORROWED);
            itemsToBorrow.add(itemToBorrow);
        }

        newLoan.setDueDate(dueDate);
        newLoan.setBorrowedAt(now);
        newLoan.setStatus(Status.pending);
        newLoan.setUser((RegisteredUser) userService.findById(userId));
        newLoan.setGamesToBeBorrowed(itemsToBorrow);

        boardGameLoanRepository.save(newLoan);

        userService.linkLoanToUser(userId, newLoan.getId());

        return newLoan.getId();
    }

    /**
     * Processes the return of a loan.
     * Updates return date, item states, loan status and user karma based on punctuality.
     *
     * @param loanId the ID of the loan being returned
     * @throws InvalidLoanReturnException if the loan is not approved or already returned
     */
    public void returnBoardGameLoan(long loanId) {
        BoardGameLoan loanToReturn = getBoardGameLoan(loanId);
        if (loanToReturn.getStatus() != Status.approved) {
            throw new InvalidLoanReturnException("Loan with id " + loanId + " is not approved and cannot be returned");
        }
        if (loanToReturn.getReturnedAt() != null) {
            throw new InvalidLoanReturnException("Loan with id " + loanId + " has already been returned");
        }

        RegisteredUser user = loanToReturn.getUser();
        LocalDateTime now = LocalDateTime.now();
        loanToReturn.setReturnedAt(now);
        if (now.isAfter(loanToReturn.getDueDate())) {
            if (user.getKarma() > 4) {
                user.setKarma(user.getKarma() - 5);
                loanToReturn.setStatus(Status.returnedLate);
            }

        } else {
            if (user.getKarma() < 91) {
                user.setKarma(user.getKarma() + 10);
                loanToReturn.setStatus(Status.returnedInTime);
            }
        }
        for (BoardGameItem boardGameItem : loanToReturn.getItems()) {
            boardGameItem.setState(BoardGameState.FOR_LOAN);
        }
    }

    /**
     * Retrieves all board game items associated with a specific loan.
     *
     * @param loanId the ID of the loan
     * @return a list of items in the loan
     * @throws EntityNotFoundException if the loan is not found
     */
    public List<BoardGameItem> getBoardGameItemsInLoan(long loanId) {
        if (!boardGameLoanRepository.existsById(loanId)) {
            throw new EntityNotFoundException(BoardGameLoan.class.getSimpleName(), loanId);
        }
        BoardGameLoan loan = getBoardGameLoan(loanId);
        return loan.getItems();
    }

    /**
     * Retrieves all loans with the status PENDING.
     *
     * @return a list of pending loans
     */
    public List<BoardGameLoan> getAllPendingLoans() {
        return boardGameLoanRepository.findAllByStatus(Status.pending);
    }

    /**
     * Retrieves all loans with the status APPROVED.
     *
     * @return a list of approved loans
     */
    public List<BoardGameLoan> getAllApprovedLoans() {
        return boardGameLoanRepository.findAllByStatus(Status.approved);
    }
}