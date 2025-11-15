package cz.cvut.fel.ear.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoardGameLoanToCreateDTO(LocalDateTime dueDate, List<String> boardGameNames, Long userId) {

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public List<String> getBoardGameNames() {
        return boardGameNames;
    }

    public Long getUserId() {
        return userId;
    }

}
