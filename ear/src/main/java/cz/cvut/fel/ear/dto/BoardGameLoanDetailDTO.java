package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.Status;

import java.time.LocalDateTime;
import java.util.List;


public record BoardGameLoanDetailDTO(Long id, LocalDateTime borrowedAt, LocalDateTime returnedAt, LocalDateTime dueDate, Status status, UserSummaryDTO user, List<BoardGameItemDTO> itemsInLoan) {
    public Long getId() {
        return id;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }
    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }
    public LocalDateTime getDueDate() {
        return dueDate;

    }

    public Status getStatus() {
        return status;
    }
    public UserSummaryDTO getUser() {
        return user;
    }
    public List<BoardGameItemDTO> getItemsInLoan() {
        return itemsInLoan;
    }

    public List<String> getBoardGameNames() {
        return itemsInLoan.stream()
                .map(BoardGameItemDTO::getName)
                .toList();
    }

}
