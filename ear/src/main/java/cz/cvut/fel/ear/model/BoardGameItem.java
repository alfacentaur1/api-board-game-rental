package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * BoardGameItem entity representing a specific board game item in a loan.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@NamedQuery(
        name = "BoardGameItem.findAvailableByNameWithLock",
        query = "SELECT e FROM BoardGameItem e " +
                "WHERE e.boardGame.name = :name " +
                "AND e.state = :state " +
                "ORDER BY e.id ASC",
        lockMode = LockModeType.PESSIMISTIC_WRITE
)
@Table(name = "board_game_items")
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
    @JoinColumn(name = "BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public BoardGameState getState() {
        return state;
    }

    public void setState(BoardGameState state) {
        this.state = state;
    }

    public BoardGame getBoardGame() {
        return boardGame;
    }

    public void setBoardGame(BoardGame boardGame) {
        this.boardGame = boardGame;
    }
}
