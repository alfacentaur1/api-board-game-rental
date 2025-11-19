package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@DiscriminatorValue("REGISTERED_USER")
public class RegisteredUser extends User {

    private int karma = 100;

    @ManyToMany
    private List<BoardGame> favoriteBoardGames = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<BoardGameLoan> boardGameLoans = new ArrayList<>();

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

    @Override
    public UserRole getRole() {
        return UserRole.ROLE_USER;
    }
}
