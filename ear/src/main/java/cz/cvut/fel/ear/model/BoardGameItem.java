package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private BoardGameState state;

    @ManyToOne
    @JoinColumn(name = "BOARD_GAME_ID", nullable = true)
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

    private String cachedGameName;

    public String getName() {
        if (boardGame != null) {
            return boardGame.getName();
        }
        return cachedGameName;
    }
    public void setCachedGameName(String cachedGameName) {
        this.cachedGameName = cachedGameName;
    }
}
