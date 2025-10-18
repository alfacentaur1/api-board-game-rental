package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class Rating {

    @Id
    @GeneratedValue
    private int id;

    private int value;
    private String comment;
    private LocalDateTime createdAt;

    //many to one has always foreign key -> owner side
    @ManyToOne
    @JoinColumn(name="USER_ID")
    private User author;

    @ManyToOne
    @JoinColumn(name="BOARD_GAME_ID")
    private BoardGame boardGame;
}
