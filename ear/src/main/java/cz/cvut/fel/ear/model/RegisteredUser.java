package cz.cvut.fel.ear.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * RegisteredUser entity representing registered users in the system.
 * Inherits from the User class.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("REGISTERED_USER")
public class RegisteredUser extends User {
    /**
     * Karma points representing users reputation and ability to borrow board games.
     */
    private int karma;

    /**
     * List of favorite board games of the registered user.
     */
    @ManyToMany
    private List<BoardGame> favoriteBoardGames;


    /**
     * List of board game loans associated with the registered user.
     */
    @OneToMany(mappedBy = "user")
    private List<BoardGameLoan> boardGameLoans;
}
