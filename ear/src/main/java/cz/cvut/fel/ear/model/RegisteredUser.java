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

@Entity
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("REGISTERED_USER")
public class RegisteredUser extends User {
    private int karma;

    @ManyToMany
    private List<BoardGame> favoriteBoardGames;

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
