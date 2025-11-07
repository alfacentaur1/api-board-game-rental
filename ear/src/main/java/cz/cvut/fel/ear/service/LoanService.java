package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidDateException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.*;
import cz.cvut.fel.ear.service.interfaces.LoanServiceI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanService implements LoanServiceI {
    private final BoardGameLoanRepository loanRepository;
    private final BoardGameItemRepository boardGameItemRepository;
    private final RegisteredUserRepository userRepository;
    private final UserService userService;


    public LoanService(BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository, RegisteredUserRepository registeredUserRepository, UserService userService) {
        this.loanRepository = boardGameLoanRepository;
        this.boardGameItemRepository = boardGameItemRepository;
        this.userRepository = registeredUserRepository;
        this.userService = userService;
    }

    @Override
    public BoardGameLoan getBoardGameLoan(long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "BoardGameLoan with id " + loanId + " not found"
                ));

    }

    @Override
    public List<BoardGameLoan> getLoans() {
        return  loanRepository.findAll();
    }

    @Override
    public List<BoardGameLoan> getUserLoans(long userId) {
        // Check if user exists
        return loanRepository.findAllByUserId(userId);
    }

    @Override
    public void approveLoan(long loanId) {
        setLoanState(loanId, LoanStatus.APPROVED);
    }

    @Override
    public void rejectLoan(long loanId) {
        setLoanState(loanId, LoanStatus.REJECTED);

        // Update board game items state back to for loan
        List<BoardGameItem> loanBoardGameItems = loanRepository.getBoardGameLoanById(loanId);
        for (BoardGameItem boardGameItem : loanBoardGameItems) {
            boardGameItem.setState(BoardGameState.FOR_LOAN);
        }
    }

    @Override
    public long createLoan(LocalDateTime dueDate, List<String> gameNames, User user) {
        LocalDateTime startDate = LocalDateTime.now();
        // Validate dueDate
        validateDueDate(dueDate, startDate);

        // Check if user wants to borrow something
        if (gameNames == null || gameNames.isEmpty()) {
            throw new ParametersException("BoardGameNames is empty");
        }

        // Create a list of borrowed Items
        List<BoardGameItem> itemsToBorrow = new ArrayList<>();

        // For each requested game name find an available item and mark it as borrowed
        for (String gameName : gameNames) {
            // Find all available items
            List<BoardGameItem> allAvailableItems = boardGameItemRepository.findAvailableByNameWithLock(gameName, BoardGameState.FOR_LOAN);

            // Check if item in stock
            if (allAvailableItems.isEmpty()) {
                throw  new EntityNotFoundException(
                        String.format("Board game %s has no available items in stock", gameName)
                );
            }

            // Take the first item in stock
            BoardGameItem newItem = allAvailableItems.get(0);

            // Make sure user borrows same game only once
            if (newItem.getState() == BoardGameState.BORROWED) {
                throw new EntityNotFoundException(
                        String.format("Board game with name %s was already borrowed in this transaction", gameName)
                );
            }

            newItem.setState(BoardGameState.BORROWED);
            itemsToBorrow.add(newItem);
        }

        // Create and persist loan with PENDING status
        BoardGameLoan loan = new BoardGameLoan();
        loan.setBorrowedAt(startDate);
        loan.setDueDate(dueDate);
        loan.setStatus(LoanStatus.PENDING);
        loan.setUser(userRepository.getReferenceById(user.getId()));

        // Set games to be borrowed // TODO - is necessary already doing in the loop
        loan.setGamesToBeBorrowed(itemsToBorrow);

        // Save new loan
        loanRepository.save(loan);

        return loan.getId();
    }

    @Override
    public void returnLoan(BoardGameLoan loan) {
        // set return time
        LocalDateTime now = LocalDateTime.now();
        loan.setReturnedAt(now);

        // Get the user
        RegisteredUser user = loan.getUser();

        // Update Loan state and user karma
        if (now.isAfter(loan.getDueDate())) {
            loan.setStatus(LoanStatus.RETURNED_LATE);
            userService.updateKarma(user, LoanStatus.RETURNED_LATE);
        } else {
            loan.setStatus(LoanStatus.RETURNED_IN_TIME);
            userService.updateKarma(user, LoanStatus.RETURNED_IN_TIME);
        }

        loanRepository.save(loan);

        // Update game items status
        for (BoardGameItem loanItem : loan.getGamesToBeBorrowed()) {
            loanItem.setState(BoardGameState.FOR_LOAN);
            boardGameItemRepository.save(loanItem);
        }
    }

    private void setLoanState(long loanId, LoanStatus newState) {
        BoardGameLoan loan = getBoardGameLoan(loanId);

        if (loan == null) {
            throw new EntityNotFoundException(
                    String.format("Loan with id %d not found", loanId)
            );
        }
        loan.setStatus(newState);

        loanRepository.save(loan);
    }

    private void validateDueDate(LocalDateTime dueDate, LocalDateTime now) {
        if(dueDate.isBefore(now)){
            throw new InvalidDateException("Due date is before current date");
        }
    }
}
