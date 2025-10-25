package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidDateException;
import cz.cvut.fel.ear.exception.InvalidStatusException;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LoanService {

    private final BoardGameLoanRepository boardGameLoanRepository;
    private final BoardGameItemRepository boardGameItemRepository;
    private final RegisteredUserRepository registeredUserRepository;
    public LoanService(BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository, RegisteredUserRepository registeredUserRepository) {
        this.boardGameLoanRepository = boardGameLoanRepository;
        this.boardGameItemRepository = boardGameItemRepository;
        this.registeredUserRepository = registeredUserRepository;
    }

    public BoardGameLoan getBoardGameLoan(long loanId) {
        return boardGameLoanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "BoardGameLoan with id " + loanId + " not found"
                ));
    }

    public List<BoardGameLoan> getBoardGameLoans() {
        return boardGameLoanRepository.findAll();
    }

    public List<BoardGameLoan> getAllBoardGameLoansByUser(long userId) {
        return boardGameLoanRepository.findAllByUserId(userId);
    }

    public void approveGameLoan(long loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        if(boardGameLoan==null){
            throw new EntityNotFoundException("BoardGameLoan with id " + loanId + " not found");
        }
        boardGameLoan.setStatus(Status.approved);
    }

    public void rejectGameLoan(long loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        if(boardGameLoan==null){
            throw new EntityNotFoundException("BoardGameLoan with id " + loanId + " not found");
        }
        List<BoardGameItem> loanBoardGameItems = boardGameLoanRepository.getBoardGameLoanById(loanId);
        for(BoardGameItem boardGameItem : loanBoardGameItems){
            boardGameItem.setState(BoardGameState.FOR_LOAN);
        }
        boardGameLoan.setStatus(Status.rejected);
    }

    public void changeLoanStatus(long loanId, Status newStatus) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        try{
            Status.valueOf(newStatus.name());
        }
        catch(IllegalArgumentException e){
            throw new InvalidStatusException("Invalid status " + newStatus.name());
        }
        if(boardGameLoan==null){
            throw new EntityNotFoundException("BoardGameLoan with id " + loanId + " not found");
        }
        boardGameLoan.setStatus(newStatus);
    }

    @Transactional
    public long createBoardGameLoan( LocalDateTime dueDate, List<String> boardGameNames, long userId) {
        LocalDateTime now = LocalDateTime.now();
        if(dueDate.isBefore(now)){
            throw new InvalidDateException("Due date is before current date");
        }
        if(now.isAfter(dueDate) ){
            throw new InvalidDateException("Borrowed date is after current date");
        }
        BoardGameLoan boardGameLoan = new BoardGameLoan();
        List<BoardGameItem> gamesToBeBorrowed = new ArrayList<>();
        for(String name : boardGameNames){
            BoardGameItem item = boardGameItemRepository
                    .findFirstByBoardGame_NameAndState(name, BoardGameState.FOR_LOAN);
            if(item==null){
                throw new EntityNotFoundException("BoardGame has no " + name + " items");
            }
            gamesToBeBorrowed.add(item);
            item.setState(BoardGameState.BORROWED);
        }
        boardGameLoan.setDueDate(dueDate);
        boardGameLoan.setBorrowedAt(now);
        boardGameLoan.setStatus(Status.pending);
        boardGameLoan.setUser(registeredUserRepository.findRegisteredUserById(userId));
        return boardGameLoanRepository.save(boardGameLoan).getId();
    }

    public void returnBoardGameLoan( BoardGameLoan boardGameLoan) {
        RegisteredUser user = boardGameLoan.getUser();

        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(boardGameLoan.getDueDate())){
            if(user.getKarma() >4) user.setKarma(user.getKarma()-5);
        }
        else {
            if(user.getKarma() <91){
                user.setKarma(user.getKarma()+10);
            }
        }
        for(BoardGameItem boardGameItem : boardGameLoan.getGamesToBeBorrowed()){
            boardGameItem.setState(BoardGameState.FOR_LOAN);
        }

    }

    public List<BoardGameItem> currentlyBorrowedBoardGameItems() {
        List<BoardGameItem> boardGameItems = boardGameItemRepository.findAll();
        List<BoardGameItem> currentlyBorrowedBoardGameItems = new ArrayList<>();;
        for(BoardGameItem boardGameItem : boardGameItems){
            if(boardGameItem.getState().equals(BoardGameState.BORROWED)){
                currentlyBorrowedBoardGameItems.add(boardGameItem);
            }
        }
        return currentlyBorrowedBoardGameItems;
    }


}
