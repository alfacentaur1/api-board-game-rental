package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.GameAlreadyInFavoritesException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.service.interfaces.BoardGameServiceI;

import java.util.Collections;
import java.util.List;

public class BoardGameService implements BoardGameServiceI {
    private final BoardGameRepository boardGameRepository;
    private final RegisteredUserRepository userRepository;
    private final BoardGameItemService boardGameItemService;


    public BoardGameService(BoardGameRepository gameRepository, BoardGameItemService gameItemService, RegisteredUserRepository userRepository) {
        this.boardGameRepository = gameRepository;
        this.boardGameItemService = gameItemService;
        this.userRepository = userRepository;

    }

    @Override
    public BoardGame getBoardGame(long id) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(id);

        // Check if board game was found
        if (boardGame == null) {
            throw new EntityNotFoundException(
                    String.format("Board game with id : %d not found", id)
            );
        }
        return boardGame;
    }

    @Override
    public List<BoardGame> getAllBoardGames() {
        List<BoardGame> boardGames = boardGameRepository.findAll();
        if (boardGames == Collections.EMPTY_LIST) {
            throw new EntityNotFoundException("There are no board games");
        }
        return boardGames;
    }

    @Override
    public long createBoardGame(String name, String description, int numberOfCopies) {
        // Check if board game with the same name already exists
        List<String> allBoardGamesNames = boardGameRepository.getAllBoardGameNames();
        if (allBoardGamesNames.contains(name)) {
            throw new EntityAlreadyExistsException(
                    String.format("Board game with title : %s already exists", name)
            );
        }

        // Check input values
        if (name == null || name.isEmpty() || description == null || description.isEmpty()) {
            throw new ParametersException("Name and description must not be empty");
        }

        // Create new board Game
        BoardGame newBoardGame = new BoardGame();
        newBoardGame.setName(name);
        newBoardGame.setDescription(description);
        boardGameRepository.save(newBoardGame);

        return newBoardGame.getId();
    }

    @Override
    public void removeBoardGame(long id) {
        // Get the board game
        BoardGame boardGameToRemove = getBoardGame(id);

        // Remove it
        boardGameRepository.delete(boardGameToRemove);
    }

    @Override
    public void updateBoardGameDescription(long id, String description) {
        BoardGame boardGameToUpdate = getBoardGame(id);

        updateDescription(boardGameToUpdate, description);
    }

    @Override
    public void updateBoardGameDescription(BoardGame boardGame, String description) {
        updateDescription(boardGame, description);
    }

    @Override
    public void printBoardGameDetail(long id) {
        BoardGame boardGame = getBoardGame(id);

        printDetail(boardGame);
    }

    @Override
    public void printBoardGameDetail(BoardGame boardGame) {
        printDetail(boardGame);
    }

    @Override
    public void addBoardGameToFavourites(RegisteredUser user, long id) {
        BoardGame boardGameToAdd = getBoardGame(id);

        // Add the boardGame
        addGameToFavourites(user, boardGameToAdd);
    }

    @Override
    public void addBoardGameToFavourites(RegisteredUser user, BoardGame boardGame) {
        addGameToFavourites(user, boardGame);
    }

    @Override
    public void removeBoardGameFromFavourites(RegisteredUser user, long id) {
        BoardGame boardGameToRemove = getBoardGame(id);

        // Remove the boardGame
        removeGameFromFavourites(user, boardGameToRemove);
    }

    @Override
    public void removeBoardGameFromFavourites(RegisteredUser user, BoardGame boardGame) {
        removeGameFromFavourites(user, boardGame);
    }

    @Override
    public List<BoardGame> getFavouriteGamesList(long userId) {
        return userRepository.findAllFavoriteGames(userId);
    }


    /**
     * Updates the description of the given board game and persists the change
     *
     * @param boardGame   board game whose description should be updated
     * @param newDescription new description to set
     */
    private void updateDescription(BoardGame boardGame, String newDescription) {
        // Check input values
        if (newDescription == null || newDescription.isEmpty()) {
            throw new ParametersException("New description must not be empty");
        }

        boardGame.setDescription(newDescription);
        boardGameRepository.save(boardGame);
    }

    /**
     * Prints detailed information about a board game to the terminal, including its name, description, and the number of available copies in stock
     *
     * @param boardGame board game to print details for
     */
    private void printDetail(BoardGame boardGame) {
        System.out.println(boardGame.getName());
        System.out.println(boardGame.getDescription());
        System.out.println("Avalaible in stock: " + boardGameItemService.gatAvailableItemsInStockNumber(boardGame.getId()));
    }

    /**
     * Adds a board game to the user's favourites list if it is not already present
     *
     * @param user      user to add the board game to
     * @param boardGame board game to add
     * @throws GameAlreadyInFavoritesException if the board game is already in the user's favourites
     */
    private void addGameToFavourites(RegisteredUser user, BoardGame boardGame) {
        // Check if user's favourite board game list already contains this board game
        if (gameInFavouriteList(user.getId(), boardGame)) {
            throw new GameAlreadyInFavoritesException(
                    String.format("Game with name %s is already if favourites", boardGame.getName())
            );
        }

        // Add the game
        user.getFavoriteBoardGames().add(boardGame);
        userRepository.save(user);
    }

    /**
     * Removes a board game from the user's favourites list if it is currently present
     *
     * @param user      user whose favourites will be modified
     * @param boardGame board game to remove
     * @throws EntityNotFoundException if the game is not found in the user's favourites
     */
    private void removeGameFromFavourites(RegisteredUser user, BoardGame boardGame) {
        // Check if game is in the user's favourite board games list
        if (!(gameInFavouriteList(user.getId(), boardGame))) {
            throw new EntityNotFoundException(
                    String.format("Game with name %s is not in favourites", boardGame.getName())
            );
        }

        // Remove the game
        user.getFavoriteBoardGames().remove(boardGame);
        userRepository.save(user);
    }

    /**
     * Checks whether a given board game is in a user's list of favourites
     *
     * @param userID      ID of the user
     * @param gameToCheck board game to check for presence in favourites
     * @return {@code true} if the board game is in the user's favourites; {@code false} otherwise
     */
    private boolean gameInFavouriteList(long userID, BoardGame gameToCheck) {
        List<BoardGame> favouriteGames = userRepository.findAllFavoriteGames(userID);
        return favouriteGames.contains(gameToCheck.getName());
    }
}