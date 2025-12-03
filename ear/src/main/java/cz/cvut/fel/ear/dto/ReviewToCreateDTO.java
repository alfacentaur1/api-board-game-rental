package cz.cvut.fel.ear.dto;

public record ReviewToCreateDTO(
        String content,
        int score
) implements BasicDTO {
}
