package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.GameAlreadyInFavoritesException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.RegisteredUser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoardGameService {
    private final BoardGameRepository boardGameRepository;
    private final UserRepository userRepository;
    private final BoardGameItemService boardGameItemService;
    private final BoardGameLoanRepository boardGameLoanRepository;

    public BoardGameService(BoardGameRepository boardGameRepository, UserRepository userRepository, BoardGameItemService boardGameItemService, BoardGameItemService boardGameItemService1, BoardGameLoanRepository boardGameLoanRepository) {
        this.boardGameRepository = boardGameRepository;
        this.userRepository = userRepository;
        this.boardGameItemService = boardGameItemService;
        this.boardGameLoanRepository = boardGameLoanRepository;
    }

    public BoardGame getBoardGame(Long gameId) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(gameId);
        if (boardGame == null) {
            throw new EntityNotFoundException("Board game not found");
        }
        return boardGame;
    }

    public List<BoardGame> getAllBoardGames() {
        List<BoardGame> boardGames = boardGameRepository.findAll();

        if (boardGames.isEmpty()) {
            throw new EntityNotFoundException("There are no board games");
        }
        return boardGames;
    }

    @Transactional
    public long createBoardGame(String name, String description) {
        List<String> allBoardGameNames = boardGameRepository.getAllBoardGameNames();
        if (allBoardGameNames.contains(name)) {
            throw new EntityAlreadyExistsException("Board game with title " + name + " already exists");
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

    @Transactional
    public void removeBoardGame(Long gameId) {
        BoardGame boardGameToRemove = boardGameRepository.getBoardGameById(gameId);

        if (boardGameToRemove == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");

        }
        boardGameRepository.delete(boardGameToRemove);
    }

    @Transactional
    public void updateBoardGameDescription(Long gameId, String newDescription) {
        BoardGame boardGameToUpdate = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToUpdate == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        if (newDescription == null || newDescription.isEmpty()) {
            throw new ParametersException("New description must not be empty");
        }
        boardGameToUpdate.setDescription(newDescription);

        boardGameRepository.save(boardGameToUpdate);
    }

    @Transactional
    public void addGameToFavorites(RegisteredUser userDTO, Long gameId) {
        if (userDTO == null) {
            throw new ParametersException("User must not be null");
        }
        // FIX: Reload user from DB to ensure entity is attached (prevents LazyInitializationException)
        RegisteredUser user = (RegisteredUser) userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        BoardGame boardGameToAdd = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToAdd == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }

        // Check using the fetched user's collection or query
        if (user.getFavoriteBoardGames().contains(boardGameToAdd)) {
            throw new GameAlreadyInFavoritesException("Game with name " + boardGameToAdd.getName() + " already in favorites");
        }

        user.getFavoriteBoardGames().add(boardGameToAdd);
        userRepository.save(user);
    }

    @Transactional
    public void removeGameFromFavorites(RegisteredUser userDTO, Long gameId) {
        if (userDTO == null) {
            throw new ParametersException("User must not be null");
        }
        // FIX: Reload user from DB
        RegisteredUser user = (RegisteredUser) userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        BoardGame boardGameToRemove = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToRemove == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }

        if (!user.getFavoriteBoardGames().contains(boardGameToRemove)) {
            throw new EntityNotFoundException("Game with name " + boardGameToRemove.getName() + " not in favorites");
        }

        user.getFavoriteBoardGames().remove(boardGameToRemove);
        userRepository.save(user);
    }

    public void viewBoardGameDetails(BoardGame boardGame) {
        if (boardGame == null) {
            throw new EntityNotFoundException("Board game not found");
        }
        System.out.println(boardGame.getName());
        System.out.println(boardGame.getDescription());
        System.out.println("Available in stock: " + boardGameItemService.availableItemsInStockNumber(boardGame.getId()));
    }

    public List<String> listAllFavoriteBoardGame(long userId) {
        return userRepository.findAllFavoriteGames(userId);
    }

    public List<BoardGame> getTopXBorrowedGames(int x) {
        if(x <= 0) {
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