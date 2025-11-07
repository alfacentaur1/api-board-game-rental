package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BoardGameItem entity representing a specific board game item in a loan.
 */
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardGameItem {
    /**
     * Unique identifier for the BoardGameItem.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * Serial number of the board game item.
     */
    @Column(nullable = false)
    private String serialNumber;

    /**
     * State of the board game item.
     */
    @Enumerated(EnumType.STRING)
    private BoardGameState state;

    /**
     * The board game represented by this item.
     */
    @ManyToOne
    @JoinColumn(name="BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;
}
