package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class BoardGameItem {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private BoardGameState state;

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
