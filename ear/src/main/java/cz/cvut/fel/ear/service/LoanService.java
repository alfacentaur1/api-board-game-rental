package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidDateException;
import cz.cvut.fel.ear.exception.InvalidStatusException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.Status;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    private final BoardGameLoanRepository boardGameLoanRepository;
    private final BoardGameItemRepository boardGameItemRepository;
    public LoanService(BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository) {
        this.boardGameLoanRepository = boardGameLoanRepository;
        this.boardGameItemRepository = boardGameItemRepository;

    }

    public BoardGameLoan getBoardGameLoan(int loanId) {
        BoardGameLoan boardGameLoan= (BoardGameLoan) boardGameLoanRepository.getBoardGameLoanById(loanId);
        if(boardGameLoan==null){
            throw new EntityNotFoundException("BoardGameLoan with id " + loanId + " not found");
        }
        return boardGameLoan;
    }

    public List<BoardGameLoan> getAllBoardGameLoans(int userId) {
        return boardGameLoanRepository.findAllByUserId()
    }

    public void approveGameLoan(int loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        if(boardGameLoan==null){
            throw new EntityNotFoundException("BoardGameLoan with id " + loanId + " not found");
        }
        boardGameLoan.setStatus(Status.approved);
    }

    public void rejectGameLoan(int loanId) {
        BoardGameLoan boardGameLoan = getBoardGameLoan(loanId);
        if(boardGameLoan==null){
            throw new EntityNotFoundException("BoardGameLoan with id " + loanId + " not found");
        }
        boardGameLoan.setStatus(Status.rejected);
    }

    public void changeLoanStatus(int loanId, Status newStatus) {
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

    public int createBoardGameLoan(LocalDateTime borrowedDate, LocalDateTime dueDate, List<String> boardGameNames) {
        LocalDateTime now = LocalDateTime.now();
        if(dueDate.isBefore(now)){
            throw new InvalidDateException("Due date is before current date");
        }
        if(borrowedDate.isAfter(dueDate) ){
            throw new InvalidDateException("Borrowed date is after current date");
        }
        BoardGameLoan boardGameLoan = new BoardGameLoan();
        for(String name : boardGameNames){

        }

    }


}
