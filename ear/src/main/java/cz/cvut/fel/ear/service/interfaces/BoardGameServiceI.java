package cz.cvut.fel.ear.service.interfaces;

import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.GameAlreadyInFavoritesException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Service providing operations for managing BoardGame entities, including
 */
public interface BoardGameServiceI {

    /**
     * Find a board game by its id
     * @param id ID of the game to find
     * @return the BoardGame with the given id
     * @throws EntityNotFoundException if the board game was not found
     */
    BoardGame getBoardGame(long id);

    /**
     * Finds all board games in the system
     * @return list of all board games; may be empty if none are present
     */
    List<BoardGame> getAllBoardGames();

    /**
     * Transactional <br>
     * Adds new board game to the system
     * @param name name of the board game
     * @param description description of the board game
     * @param numberOfCopies number of available copies
     * @return id of the newly added game
     * @throws EntityAlreadyExistsException when a game with the same id already exists
     */
    @Transactional
    long createBoardGame(String name, String description, int numberOfCopies);

    /**
     * Transactional <br>
     * Removes a board game from the system
     * @param id id of the board game to remove
     * @throws EntityNotFoundException when there is no board game with the given id
     */
    void removeBoardGame(long id);

    /**
     * Transactional <br>
     * Updates description of a board game
     * @param id id of the board game to edit
     * @param description new description
     * @throws EntityNotFoundException when there is no board game with the given id
     */
    @Transactional
    void updateBoardGameDescription(long id, String description);

    /**
     * Transactional <br>
     * Updates description of a board game
     * @param boardGame board game to edit description for
     * @param description new description
     * @throws EntityNotFoundException when there is no board game with the given id
     */
    @Transactional
    void updateBoardGameDescription(BoardGame boardGame, String description);

    /**
     * Prints board game details to the terminal by looking up the game by id
     * @param id id of the board game
     * @throws EntityNotFoundException when there is no board game with the given id
     */
    void printBoardGameDetail(long id);

    /**
     * Prints board game details to the terminal for the provided BoardGame instance
     * Prints board game details to the terminal
     * @param boardGame board game to print detail
     * @throws EntityNotFoundException when there is no board game matching with given id in the system
     */
    void printBoardGameDetail(BoardGame boardGame);

    /**
     * Transactional <br>
     * Adds a board game to the user's favourites by board game id
     * @param user user to add the board game to
     * @param id ID of the board game to add to favourites
     * @throws EntityNotFoundException when there is no board game with the given id
     * @throws GameAlreadyInFavoritesException when the game already is in the user's favourite games list
     */
    @Transactional
    void addBoardGameToFavourites(RegisteredUser user, long id);

    /**
     * Transactional <br>
     * Adds a board game to the user's favourites using an already-loaded BoardGame
     * @param user user to add the board game to
     * @param boardGame board game to add to favourites
     * @throws GameAlreadyInFavoritesException when the game already is in the user's favourite games list
     */
    @Transactional
    void addBoardGameToFavourites(RegisteredUser user, BoardGame boardGame);

    /**
     * Transactional <br>
     * Removes a board game from the user's favourites by board game id
     * @param user user to remove the board game from
     * @param id ID of the board game to remove from favourites
     * @throws EntityNotFoundException when there is no board game with the given id
     */
    @Transactional
    void removeBoardGameFromFavourites(RegisteredUser user, long id);

    /**
     * Transactional <br>
     * Removes a board game from the user's favourites using an already-loaded BoardGame
     * @param user user to remove the board game from
     * @param boardGame board game to remove from favourites
     * @throws EntityNotFoundException when there is no board game with the given id
     */
    @Transactional
    void removeBoardGameFromFavourites(RegisteredUser user, BoardGame boardGame);

    /**
     * Finds list of favourite board games for user
     *
     * @param userId Id of the user to find list for
     * @return list of boardGames in the favourite games list
     */
    List<BoardGame> getFavouriteGamesList(long userId);


}
