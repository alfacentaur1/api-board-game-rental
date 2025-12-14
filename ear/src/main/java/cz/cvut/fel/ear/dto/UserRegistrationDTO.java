package cz.cvut.fel.ear.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRegistrationDTO(
        @NotBlank String fullName,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String username
) implements BasicDTO {
}
