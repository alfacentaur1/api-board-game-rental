package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(
        @NotBlank String username,
        @NotBlank String password
) implements BasicDTO {
}
