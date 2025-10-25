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
public class Review {

    @Id
    @GeneratedValue
    private long id;

    //rating
    private int value;
    private String comment;
    private LocalDateTime createdAt;

    //many to one has always foreign key -> owner side
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "BOARD_GAME_ID", nullable = false)
    private BoardGame boardGame;
}
