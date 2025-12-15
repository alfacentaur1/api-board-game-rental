package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotNull;


public record FavoriteCreationDTO(
        @NotNull Long gameId
) implements BasicDTO{}