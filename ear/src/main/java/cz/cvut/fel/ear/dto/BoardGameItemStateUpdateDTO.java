package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;
import jakarta.validation.constraints.NotNull;

public record BoardGameItemStateUpdateDTO(
        @NotNull Long itemId,
        @NotNull BoardGameState state
) implements BasicDTO {}