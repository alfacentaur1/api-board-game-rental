package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardGameItemCreationDTO(
        @NotNull Long boardGameId,
        @NotBlank String serialNumber,
        @NotNull BoardGameState state
) implements BasicDTO{
}
