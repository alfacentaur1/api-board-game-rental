package cz.cvut.fel.ear.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoardGameLoanToCreateDTO(LocalDateTime dueDate, List<String> boardGameNames, Long userId) implements BasicDTO {


}
