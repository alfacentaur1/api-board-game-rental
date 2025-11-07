package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidDateException;
import cz.cvut.fel.ear.exception.NotAvalaibleInStockException;
import cz.cvut.fel.ear.model.*;
import cz.cvut.fel.ear.service.interfaces.LoanServiceI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanService implements LoanServiceI {
    private final BoardGameLoanRepository loanRepository;
    private final BoardGameItemRepository itemRepository;
    private final RegisteredUserRepository userRepository;
    private final UserService userService;


    public LoanService(BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository, RegisteredUserRepository registeredUserRepository, UserService userService) {
        this.loanRepository = boardGameLoanRepository;
        this.itemRepository = boardGameItemRepository;
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
    }

    @Override
    public long createLoan(LocalDateTime dueDate, List<String> gameNames, User user) {
        LocalDateTime startDate = LocalDateTime.now();
        // Validate dueDate
        validateDueDate(dueDate, startDate);

        // Create a list of borrowed Items
        List<BoardGameItem> gamesToBorrow = new ArrayList<>();

        // For each requested game name find an available item and mark it as borrowed
        for (String gameName : gameNames) {
            BoardGameItem item = itemRepository.findFirstByBoardGame_NameAndState(gameName, BoardGameState.FOR_LOAN);
            if (item == null) {
                throw new NotAvalaibleInStockException("Game '" + gameName + "' is not available in stock");
            }
            item.setState(BoardGameState.BORROWED);
            itemRepository.save(item);
            gamesToBorrow.add(item);
        }

        // Create and persist loan with PENDING status
        BoardGameLoan loan = new BoardGameLoan();
        loan.setBorrowedAt(startDate);
        loan.setDueDate(dueDate);
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setUser(userRepository.findRegisteredUserById(user.getId()));

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
        if (loan.getDueDate() != null && now.isAfter(loan.getDueDate())) {
            loan.setLoanStatus(LoanStatus.RETURNED_LATE);
            userService.updateKarma(user, LoanStatus.RETURNED_LATE);

        } else {
            loan.setLoanStatus(LoanStatus.RETURNED_IN_TIME);
            userService.updateKarma(user, LoanStatus.RETURNED_IN_TIME);
        }
        loanRepository.save(loan);

        // Update game items status
        for (BoardGameItem loanItem : loan.getGamesToBeBorrowed()) {
            loanItem.setState(BoardGameState.FOR_LOAN);
            itemRepository.save(loanItem);
        }
    }

    private void setLoanState(long loanId, LoanStatus newState) {
        BoardGameLoan loan = getBoardGameLoan(loanId);

        if (loan == null) {
            throw new EntityNotFoundException(
                    String.format("Loan with id %d not found", loanId)
            );
        }
        loan.setLoanStatus(newState);

        loanRepository.save(loan);
    }

    private void validateDueDate(LocalDateTime dueDate, LocalDateTime now) {
        if(dueDate.isBefore(now)){
            throw new InvalidDateException("Due date is before current date");
        }
        if(now.isAfter(dueDate) ){
            throw new InvalidDateException("Borrowed date is after current date");
        }
    }


}
