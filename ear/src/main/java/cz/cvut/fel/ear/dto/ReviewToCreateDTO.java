package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewToCreateDTO(
        @NotNull Long gameId,
        String content,
        @NotNull int score
) implements BasicDTO {
}