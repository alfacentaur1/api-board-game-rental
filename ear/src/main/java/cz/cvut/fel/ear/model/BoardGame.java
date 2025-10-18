package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardGame {
    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String name;

    private String description;
    private int availableCopies;

    //mapping composition here
    @OneToMany(mappedBy = "boardGame",cascade = CascadeType.ALL)
    private List<Rating> ratings;

    @OneToMany(mappedBy = "boardGame")
    private List<BoardGameItem> availableStockItems;

    @ManyToMany
    @JoinTable(
            name = "BOARDGAME_CATEGORY",
            joinColumns = @JoinColumn(name = "BOARDGAME_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID")
    )
    private List<Category> categories;
}
