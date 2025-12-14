package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreationDTO(
        @NotBlank String name
) implements BasicDTO {

}
