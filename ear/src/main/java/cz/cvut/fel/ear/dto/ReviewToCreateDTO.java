package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewToCreateDTO(
        @NotNull Long userId,
        @NotNull Long gameId,
        String content,
        int score
) implements BasicDTO {
}