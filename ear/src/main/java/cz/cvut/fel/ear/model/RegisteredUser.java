package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
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

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public List<BoardGame> getFavoriteBoardGames() {
        return favoriteBoardGames;
    }

    public void setFavoriteBoardGames(List<BoardGame> favoriteBoardGames) {
        this.favoriteBoardGames = favoriteBoardGames;
    }

    public List<BoardGameLoan> getBoardGameLoans() {
        return boardGameLoans;
    }

    public void setBoardGameLoans(List<BoardGameLoan> boardGameLoans) {
        this.boardGameLoans = boardGameLoans;
    }
}
