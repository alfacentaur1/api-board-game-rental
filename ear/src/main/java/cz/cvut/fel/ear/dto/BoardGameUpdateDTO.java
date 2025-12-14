package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardGameUpdateDTO(
        @NotNull Long id,
        @NotBlank String description
) implements BasicDTO{
}
