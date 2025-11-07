package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Review entity representing a review for a board game.
 */
@Getter
@Setter
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class Review {

    /**
     * Unique identifier for the review.
     */
    @Id
    @GeneratedValue
    private long id;

    //rating
    /**
     * Value of the review rating.
     */
    private int value;
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
    @JoinColumn(name="USER_ID", nullable = false)
    private User author;

    /**
     * The board game being reviewed.
     */
    @ManyToOne
    @JoinColumn(name="BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;
}
