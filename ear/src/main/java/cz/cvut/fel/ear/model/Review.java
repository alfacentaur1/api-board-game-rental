package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review entity representing a review for a board game.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    /**
     * Unique identifier for the review.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * Value of the review rating.
     */
    private int score;

    /**
     * Rating comment
     */
    private String comment;
    /**
     * LocalDateTime when the review was created.
     */
    private LocalDateTime createdAt;


    //many to one has always foreign key -> owner side
    /**
     * The user who authored the review.
     */
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User author;

    /**
     * The board game being reviewed.
     */
    @ManyToOne
    @JoinColumn(name = "BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int value) {
        this.score = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public BoardGame getBoardGame() {
        return boardGame;
    }

    public void setBoardGame(BoardGame boardGame) {
        this.boardGame = boardGame;
    }
}
