package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record FavoriteCreationDTO(
        @NotNull String username,
        @NotNull Long gameId
) implements BasicDTO{}