package cz.cvut.fel.ear.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BoardGameLoanToCreateDTO(
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dueDate,
        @NotEmpty List<String> boardGameNames
) implements BasicDTO {
}
