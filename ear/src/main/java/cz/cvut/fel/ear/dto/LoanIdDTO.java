package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotNull;

public record LoanIdDTO(
        @NotNull Long loanId
) implements BasicDTO{}