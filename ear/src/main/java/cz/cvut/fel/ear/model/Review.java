package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue
    private long id;

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

    //rating
    private int score;
    private String comment;
    private LocalDateTime createdAt;

    //many to one has always foreign key -> owner side
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;

    public Review(int score, String comment, LocalDateTime createdAt, User author, BoardGame boardGame) {
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
        this.author = author;
        this.boardGame = boardGame;
    }
    public Review(){}

    public RegisteredUser getAuthorAsRegisteredUser() {
        if (author instanceof RegisteredUser) {
            return (RegisteredUser) author;
        } else {
            throw new IllegalStateException("Author is not a RegisteredUser");
        }
    }
}
