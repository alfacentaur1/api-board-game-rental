package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BoardGameLoan entity representing a loan of board games to a registered user.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="board_game_loans")
public class BoardGameLoan {
    /**
     * Unique identifier for the board game loan.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * LocalDateTime when the board games were borrowed.
     */
    private LocalDateTime borrowedAt;
    /**
     * LocalDateTime when the board games were returned.
     */
    private LocalDateTime returnedAt;
    /**
     * LocalDateTime when the board games are due to be returned.
     */
    private LocalDateTime dueDate;

    /**
     * Status of the loan.
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The registered user who borrowed the board games.
     */
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private RegisteredUser user;

    /**
     * List of board game items that are part of this loan.
     */
    @ManyToMany
    private List<BoardGameItem> gamesToBeBorrowed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public RegisteredUser getUser() {
        return user;
    }

    public void setUser(RegisteredUser user) {
        this.user = user;
    }

    public List<BoardGameItem> getGamesToBeBorrowed() {
        return gamesToBeBorrowed;
    }

    public void setGamesToBeBorrowed(List<BoardGameItem> gamesToBeBorrowed) {
        this.gamesToBeBorrowed = gamesToBeBorrowed;
    }
}
