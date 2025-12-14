package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotBlank;

public record BoardGameToCreateDTO(
        @NotBlank String name,
        @NotBlank String description
) implements BasicDTO {
}
