package cz.cvut.fel.ear.dto;
import java.time.LocalDateTime;

public record ReviewDetailDTO(int score, String comment, String authorName, String boardGameTitle, LocalDateTime createdAt) implements BasicDTO {

}
