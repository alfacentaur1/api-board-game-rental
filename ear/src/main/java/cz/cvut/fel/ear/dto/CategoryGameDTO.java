package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryGameDTO(
        @NotNull Long categoryId,
        @NotNull Long boardGameId
) implements BasicDTO{
}