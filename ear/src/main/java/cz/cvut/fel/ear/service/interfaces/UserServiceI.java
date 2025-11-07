package cz.cvut.fel.ear.service.interfaces;

import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Status;
import cz.cvut.fel.ear.model.RegisteredUser;

import java.util.List;

public interface UserServiceI {

    /**
     * Updates the karma score of a user based on a loan status
     * @param user registered user whose karma to update
     * @param status the loan status that determines how to update karma points
     */
    void updateKarma(RegisteredUser user, Status status);

    public List<BoardGame> getAllFavouriteGames(long userId);
}
