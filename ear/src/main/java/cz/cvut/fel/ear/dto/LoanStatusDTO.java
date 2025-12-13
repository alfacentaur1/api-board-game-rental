package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.Status;
import jakarta.validation.constraints.NotNull;

public record LoanStatusDTO(
        @NotNull
        Long loanId,
        @NotNull
        Status status
) implements BasicDTO {}