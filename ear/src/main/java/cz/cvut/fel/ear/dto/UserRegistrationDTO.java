package cz.cvut.fel.ear.dto;

public record UserRegistrationDTO(String fullName, String email, String password, String username) implements BasicDTO {
}
