package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Category entity representing a category of board games.
 */
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    /**
     * List of board games associated with this category.
     */
    @ManyToMany(mappedBy = "categories")
    private List<BoardGame> boardGames;
}
