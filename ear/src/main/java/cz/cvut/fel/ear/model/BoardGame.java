package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * BoardGame entity representing a board game in the system.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="board_games")
public class BoardGame {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public List<Review> getRatings() {
        return ratings;
    }

    public void setRatings(List<Review> ratings) {
        this.ratings = ratings;
    }

    public List<BoardGameItem> getAvailableStockItems() {
        return availableStockItems;
    }

    public void setAvailableStockItems(List<BoardGameItem> availableStockItems) {
        this.availableStockItems = availableStockItems;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

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
    private String description = "";

    /**
     * The amount of available copies of the board game.
     */
    private int availableCopies = 0;

    /**
     * List of reviews associated with the board game.
     */
    @OneToMany(mappedBy = "boardGame", cascade = CascadeType.ALL)
    private List<Review> ratings = new ArrayList<>();

    /**
     * List of available board game items in stock.
     */
    @OneToMany(mappedBy = "boardGame")
    private List<BoardGameItem> availableStockItems = new ArrayList<>();

    /**
     * List of categories associated with the board game.
     */
    @ManyToMany
    @JoinTable(
            name = "BOARDGAME_CATEGORY",
            joinColumns = @JoinColumn(name = "BOARDGAME_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID")
    )
    private List<Category> categories = new ArrayList<>();
}
