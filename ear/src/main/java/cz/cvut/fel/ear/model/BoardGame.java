package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
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

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description = "";

    @Formula("(SELECT COUNT(*) FROM board_game_items bgi WHERE bgi.board_game_id = id AND bgi.state = 'FOR_LOAN')")
    private int availableCopies;

    //mapping composition here
    @OneToMany(mappedBy = "boardGame", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private List<Review> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "boardGame", cascade = CascadeType.ALL)
    private List<BoardGameItem> availableStockItems = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "BOARDGAME_CATEGORY",
            joinColumns = @JoinColumn(name = "BOARDGAME_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID")
    )
    private List<Category> categories = new ArrayList<>();

    public BoardGame(String name) {
        this.name = name;
        this.ratings = new ArrayList<>();
        this.availableStockItems = new ArrayList<>();
        this.categories = new ArrayList<>();
    }
    public BoardGame(){}
}
