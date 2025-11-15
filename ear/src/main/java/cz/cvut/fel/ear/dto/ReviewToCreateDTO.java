package cz.cvut.fel.ear.dto;

public record ReviewToCreateDTO(
        String content,
        int score
) {
    public int getScore() {
        return score;
    }

    public String getContent() {
        return content;
    }
}
