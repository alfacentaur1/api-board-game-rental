package cz.cvut.fel.ear.dto;
import java.time.LocalDateTime;

public record ReviewDetailDTO(int score, String comment, String authorName, String boardGameTitle, LocalDateTime createdAt) {
    public int getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }
    public String getAuthorName() {
        return authorName;
    }
    public String getBoardGameTitle() {
        return boardGameTitle;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
