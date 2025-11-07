package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * BoardGame entity representing a board game in the system.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardGame {
    /**
     * Unique identifier for the board game.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * Name of the board game.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Description of the board game.
     */
    private String description;

    /**
     * The amount of available copies of the board game.
     */
    private int availableCopies;

    //mapping composition here

    /**
     * List of reviews associated with the board game.
     */
    @OneToMany(mappedBy = "boardGame",cascade = CascadeType.ALL)
    private List<Review> ratings;

    /**
     * List of available board game items in stock.
     */
    @OneToMany(mappedBy = "boardGame")
    private List<BoardGameItem> availableStockItems;

    /**
     * List of categories associated with the board game.
     */
    @ManyToMany
    @JoinTable(
            name = "BOARDGAME_CATEGORY",
            joinColumns = @JoinColumn(name = "BOARDGAME_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID")
    )
    private List<Category> categories;
}
