package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Status;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.service.interfaces.BoardGameServiceI;
import cz.cvut.fel.ear.service.interfaces.UserServiceI;

import java.util.ArrayList;
import java.util.List;

public class UserService implements UserServiceI {
    private final int KARMA_UP = 10;
    private final int KARMA_DOWN = 5;
    private final int KARMA_MAX = 100;

    private final RegisteredUserRepository userRepository;
    private final BoardGameServiceI gameService;

    public UserService(RegisteredUserRepository userRepository, BoardGameService gameService) {
        this.userRepository = userRepository;
        this.gameService = gameService;
    }

    @Override
    public void updateKarma(RegisteredUser user, Status status) {
        if (status == Status.RETURNED_LATE && (user.getKarma() > 4)) {
            user.setKarma(user.getKarma() - KARMA_DOWN);
        } else if (status == Status.RETURNED_IN_TIME && (user.getKarma() < 91)) {
            user.setKarma(user.getKarma() + KARMA_UP);
        } else {
            user.setKarma(KARMA_MAX);
        }
    }

    @Override
    public List<BoardGame> getAllFavouriteGames(long userId) {
        // TODO - override function that will return ids from db
        List<String> gameNames = userRepository.findAllFavoriteGames(userId);

        List<BoardGame> favouriteGames = new ArrayList<>();

        // TODO - implement function that will find board games based on ids from db
        /*for (String name : gameNames) {
            BoardGame favouriteGame = gameService.getBoardGame()
        }*/
        return new ArrayList<>();
    }
}
