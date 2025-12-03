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
public class  LoanService {

    private final BoardGameLoanRepository boardGameLoanRepository;
    private final BoardGameItemRepository boardGameItemRepository;
    private final UserService userService;

    public LoanService(BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository, @Lazy UserService userService) {
        this.boardGameLoanRepository = boardGameLoanRepository;
        this.boardGameItemRepository = boardGameItemRepository;
        this.userService = userService;
    }

    public BoardGameLoan getBoardGameLoan(long loanId) {
        return boardGameLoanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException(BoardGameLoan.class.getSimpleName(), loanId));
    }

    public List<BoardGameLoan> getBoardGameLoans() {
        return boardGameLoanRepository.findAll();
    }

    public List<BoardGameLoan> getAllBoardGameLoansByUser(long userId) {
        List<BoardGameLoan> loans = boardGameLoanRepository.findAllByUserId(userId);
        if (loans == null) {
            throw new EntityNotFoundException(BoardGameLoan.class.getSimpleName(), null, User.class.getSimpleName(), userId);
        }
        return loans;
    }

    public void approveGameLoan(long loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        boardGameLoan.setStatus(Status.approved);
    }

    public void rejectGameLoan(long loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        List<BoardGameItem> loanBoardGameItems = boardGameLoan.getItems();
        for (BoardGameItem boardGameItem : loanBoardGameItems) {
            boardGameItem.setState(BoardGameState.FOR_LOAN);
        }
        boardGameLoan.setStatus(Status.rejected);
    }

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

    @Transactional
    public long createLoan(LocalDateTime dueDate, List<String> boardGameNames, long userId) {
        // Validate due date
        LocalDateTime now = LocalDateTime.now();

        if (dueDate.isBefore(now)) {
            throw new InvalidDateException("Due date is before current date");
        }

        // Check if there are any games to be borrowed
        if (boardGameNames == null || boardGameNames.isEmpty()) {
            throw new ParametersException("There are no board games to borrow (boardGameNames is empty");
        }

        BoardGameLoan newLoan = new BoardGameLoan();
        List<BoardGameItem> itemsToBorrow = new ArrayList<>();

        for (String name : boardGameNames) {
            // Get all available board game items
            List<BoardGameItem> allAvailableItems = boardGameItemRepository.findAvailableByNameWithLock(name, BoardGameState.FOR_LOAN);

            // Check if any items were found
            if (allAvailableItems.isEmpty()) {
                throw new NotAvalaibleInStockException(
                        String.format("Board game %s has no available items to borrow", name)
                );
            }

            // Borrow the first item in items list
            BoardGameItem itemToBorrow = allAvailableItems.getFirst();

            // Set the state of the item to borrowed
            itemToBorrow.setState(BoardGameState.BORROWED);
            itemsToBorrow.add(itemToBorrow);
        }

        // Set loan details
        newLoan.setDueDate(dueDate);
        newLoan.setBorrowedAt(now);
        newLoan.setStatus(Status.pending);
        newLoan.setUser((RegisteredUser) userService.findById(userId));
        newLoan.setGamesToBeBorrowed(itemsToBorrow);

        boardGameLoanRepository.save(newLoan);

        // Bind loan to the user
        userService.linkLoanToUser(userId, newLoan.getId());

        return newLoan.getId();
    }

    public void returnBoardGameLoan(long loanId) {
        BoardGameLoan loanToReturn = getBoardGameLoan(loanId);
        if(loanToReturn.getStatus() != Status.approved){
            throw new InvalidLoanReturnException("Loan with id " + loanId + " is not approved and cannot be returned");
        }
        if(loanToReturn.getReturnedAt() != null){
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

    public List<BoardGameItem> getBoardGameItemsInLoan(long loanId) {
        if(!boardGameLoanRepository.existsById(loanId)) {
            throw new EntityNotFoundException(BoardGameLoan.class.getSimpleName(), loanId);
        }
        BoardGameLoan loan = getBoardGameLoan(loanId);
        return loan.getItems();
    }

    public List<BoardGameLoan> getAllPendingLoans() {
        return boardGameLoanRepository.findAllByStatus(Status.pending);
    }

    public List<BoardGameLoan> getAllApprovedLoans() {
        return boardGameLoanRepository.findAllByStatus(Status.approved);
}}
