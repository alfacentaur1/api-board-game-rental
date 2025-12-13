package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.exception.*;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BoardGameService {
    private final BoardGameRepository boardGameRepository;
    private final UserRepository userRepository;
    private final BoardGameItemService boardGameItemService;
    private final BoardGameLoanRepository boardGameLoanRepository;
    private final BoardGameItemRepository boardGameItemRepository;

    public BoardGameService(BoardGameRepository boardGameRepository, UserRepository userRepository, BoardGameItemService boardGameItemService, BoardGameItemService boardGameItemService1, BoardGameLoanRepository boardGameLoanRepository, BoardGameItemRepository boardGameItemRepository) {
        this.boardGameRepository = boardGameRepository;
        this.userRepository = userRepository;
        this.boardGameItemService = boardGameItemService;
        this.boardGameLoanRepository = boardGameLoanRepository;
        this.boardGameItemRepository = boardGameItemRepository;
    }

    /**
     * Retrieves a board game by its unique identifier.
     *
     * @param gameId the ID of the board game to retrieve
     * @return the found BoardGame entity
     * @throws EntityNotFoundException if no board game with the given ID is found
     */
    public BoardGame getBoardGame(Long gameId) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(gameId);
        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), gameId);
        }
        return boardGame;
    }

    /**
     * Retrieves all board games available in the system.
     *
     * @return a list of all BoardGame entities
     * @throws EntityNotFoundException if no board games are found in the system
     */
    public List<BoardGame> getAllBoardGames() {
        List<BoardGame> boardGames = boardGameRepository.findAll();

        if (boardGames.isEmpty()) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), null);
        }

        return boardGames;
    }

    /**
     * Creates a new board game with the specified name and description.
     *
     * @param name        the name of the board game
     * @param description the description of the board game
     * @return the ID of the newly created board game
     * @throws EntityAlreadyExistsException if a board game with the same name already exists
     * @throws ParametersException      if the name or description is null or empty
     */
    @Transactional
    public long createBoardGame(String name, String description) {
        List<String> allBoardGameNames = boardGameRepository.getAllBoardGameNames();
        if (allBoardGameNames.contains(name)) {
            throw new EntityAlreadyExistsException(BoardGame.class.getSimpleName(), name);
        }
        if (name == null || name.isEmpty() || description == null || description.isEmpty()) {
            throw new ParametersException("Name and description must not be empty");
        }
        BoardGame boardGameToCreate = new BoardGame();
        boardGameToCreate.setName(name);
        boardGameToCreate.setDescription(description);
        boardGameRepository.save(boardGameToCreate);
        return boardGameToCreate.getId();
    }

    /**
     * Removes a board game from the system by its ID.
     *
     * @param gameId the ID of the board game to remove
     * @throws EntityNotFoundException   if no board game with the given ID is found
     * @throws EntityReferenceException  if any copies of the game are currently borrowed
     */
    @Transactional
    public void removeBoardGame(Long gameId) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(gameId);

        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), gameId);
        }

        boolean isCurrentlyBorrowed = boardGame.getAvailableStockItems().stream()
                .anyMatch(item -> item.getState() == BoardGameState.BORROWED);

        if (isCurrentlyBorrowed) {
            throw new EntityReferenceException("Game cannot be deleted, some of the copies are still borrowed.");
        }

        for (BoardGameItem item : boardGame.getAvailableStockItems()) {
            item.setCachedGameName(boardGame.getName());
            item.setBoardGame(null);

            boardGameItemRepository.save(item);
        }
        boardGame.getAvailableStockItems().clear();
        boardGameRepository.delete(boardGame);
    }

    /**
     * Updates the description of an existing board game.
     *
     * @param gameId         the ID of the board game to update
     * @param newDescription the new description to set
     * @throws EntityNotFoundException if no board game with the given ID is found
     * @throws ParametersException     if the new description is null or empty
     */
    @Transactional
    public void updateBoardGameDescription(Long gameId, String newDescription) {
        BoardGame boardGameToUpdate = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToUpdate == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), gameId);
        }
        if (newDescription == null || newDescription.isEmpty()) {
            throw new ParametersException("New description must not be empty");
        }
        boardGameToUpdate.setDescription(newDescription);

        boardGameRepository.save(boardGameToUpdate);
    }

    /**
     * Adds a board game to the user's list of favorite games.
     *
     * @param userDTO the user who wants to add the game to favorites
     * @param gameId  the ID of the board game to add
     * @throws ParametersException             if the user is null
     * @throws EntityNotFoundException         if the user or board game is not found
     * @throws GameAlreadyInFavoritesException if the game is already in the user's favorites
     */
    @Transactional
    public void addGameToFavorites(RegisteredUser userDTO, Long gameId) {
        if (userDTO == null) {
            throw new ParametersException("User must not be null");
        }
        RegisteredUser user = (RegisteredUser) userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(User.class.getSimpleName(), userDTO.getId()));

        BoardGame boardGameToAdd = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToAdd == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), gameId);
        }

        if (user.getFavoriteBoardGames().contains(boardGameToAdd)) {
            throw new GameAlreadyInFavoritesException();
        }

        user.getFavoriteBoardGames().add(boardGameToAdd);
        userRepository.save(user);
    }

    /**
     * Removes a board game from the user's list of favorite games.
     *
     * @param userDTO the user who wants to remove the game from favorites
     * @param gameId  the ID of the board game to remove
     * @throws ParametersException     if the user is null
     * @throws EntityNotFoundException if the user is not found
     * @throws ItemNotInResource       if the game is not in the user's favorites
     */
    @Transactional
    public void removeGameFromFavorites(RegisteredUser userDTO, Long gameId) {
        if (userDTO == null) {
            throw new ParametersException("User must not be null");
        }
        RegisteredUser user = (RegisteredUser) userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(User.class.getSimpleName(), userDTO.getId()));

        BoardGame boardGameToRemove = getBoardGame(gameId);

        if (user.getFavoriteBoardGames().contains(boardGameToRemove)) {
            user.getFavoriteBoardGames().remove(boardGameToRemove);
            userRepository.save(user);
        } else {
            throw new ItemNotInResource("BoardGame", "Favourites");
        }
    }

    /**
     * Prints details of a board game to the console.
     *
     * @param boardGame the board game to view
     * @throws EntityNotFoundException if the board game is null
     */
    public void viewBoardGameDetails(BoardGame boardGame) {
        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), null);
        }
        System.out.println(boardGame.getName());
        System.out.println(boardGame.getDescription());
        System.out.println("Available in stock: " + boardGameItemService.availableItemsInStockNumber(boardGame.getId()));
    }

    /**
     * Lists the names of all favorite board games for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of names of favorite board games
     */
    public List<String> listAllFavoriteBoardGame(long userId) {
        return userRepository.findAllFavoriteGames(userId);
    }

    /**
     * Retrieves the top X most borrowed board games.
     *
     * @param x the number of games to retrieve
     * @return a list of the top X borrowed board games
     * @throws ParametersException if x is less than or equal to 0
     */
    public List<BoardGame> getTopXBorrowedGames(int x) {
        if (x <= 0) {
            throw new ParametersException("Parameter x must be greater than 0");
        }

        List<BoardGameLoan> allLoans = boardGameLoanRepository.findAll();

        Map<BoardGame, Integer> gameCounts = new HashMap<>();

        for (BoardGameLoan loan : allLoans) {
            for (BoardGameItem item : loan.getItems()) {

                BoardGame game = item.getBoardGame();

                if (game != null) {
                    gameCounts.put(game, gameCounts.getOrDefault(game, 0) + 1);
                }
            }
        }

        return gameCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(x)
                .collect(Collectors.toList());
    }
}