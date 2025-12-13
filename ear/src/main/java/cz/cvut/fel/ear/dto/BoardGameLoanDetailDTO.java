package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public record BoardGameLoanDetailDTO(Long id, LocalDate borrowedAt, LocalDate returnedAt, LocalDate dueDate, Status status, UserSummaryDTO user, List<BoardGameItemDTO> itemsInLoan) implements BasicDTO {

    public List<String> getBoardGameNames() {
        return itemsInLoan.stream()
                .map(BoardGameItemDTO::getName)
                .toList();
    }

}
