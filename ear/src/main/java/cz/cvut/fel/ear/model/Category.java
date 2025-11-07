package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Category entity representing a category of board games.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="categories")
public class Category {
    /**
     * Unique identifier for the category.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * Name of the category.
     */
    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "categories", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    /**
     * List of board games associated with this category.
     */
    private List<BoardGame> boardGames;

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

    public List<BoardGame> getBoardGames() {
        return boardGames;
    }

    public void setBoardGames(List<BoardGame> boardGames) {
        this.boardGames = boardGames;
    }
}
