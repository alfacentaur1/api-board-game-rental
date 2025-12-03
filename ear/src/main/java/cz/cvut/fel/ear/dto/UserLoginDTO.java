package cz.cvut.fel.ear.dto;

public record UserLoginDTO(String username, String password) implements BasicDTO {
}
