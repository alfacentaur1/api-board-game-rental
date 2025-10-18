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
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("REGISTERED_USER")
public class RegisteredUser extends User {
    private int karma;

    @ManyToMany
    private List<BoardGame> favoriteBoardGames;

    @OneToMany(mappedBy = "user")
    private List<BoardGameLoan> boardGameLoans;
}
