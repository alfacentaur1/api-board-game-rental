package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;
import jakarta.validation.constraints.NotNull;

public record BoardGameItemStateDTO(
        @NotNull BoardGameState boardGameState,
        @NotNull Long id
) implements BasicDTO {
}
