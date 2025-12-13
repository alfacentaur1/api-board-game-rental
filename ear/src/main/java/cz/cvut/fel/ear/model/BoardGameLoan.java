package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="board_game_loans")
public class BoardGameLoan {
    @Id
    @GeneratedValue
    private long id;

    private LocalDate borrowedAt;
    private LocalDate returnedAt;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private RegisteredUser user;

    @ManyToMany
    private List<BoardGameItem> itemsInLoan = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDate borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
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

    public List<BoardGameItem> getItems() {
        return itemsInLoan;
    }

    public void setGamesToBeBorrowed(List<BoardGameItem> gamesToBeBorrowed) {
        this.itemsInLoan = gamesToBeBorrowed;
    }
}
