package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BoardGameLoan entity representing a loan of board games to a registered user.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    private LoanStatus loanStatus;

    /**
     * The registered user who borrowed the board games.
     */
    @ManyToOne
    @JoinColumn(name="USER_ID",nullable=false)
    private RegisteredUser user;

    /**
     * List of board game items that are part of this loan.
     */
    @ManyToMany
    private List<BoardGameItem> gamesToBeBorrowed;
}
