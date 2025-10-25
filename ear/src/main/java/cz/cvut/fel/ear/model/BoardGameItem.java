package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardGameItem {
    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private BoardGameState state;

    @ManyToOne
    @JoinColumn(name="BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;
}
