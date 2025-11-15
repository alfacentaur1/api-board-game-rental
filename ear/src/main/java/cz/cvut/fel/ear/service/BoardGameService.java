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
import jakarta.transaction.Transactional; // Import this
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BoardGameService {
    private final BoardGameRepository boardGameRepository;
    private final RegisteredUserRepository userRepository;
    private final BoardGameItemRepository boardGameItemRepository;
    private final BoardGameItemService boardGameItemService;

    public BoardGameService(BoardGameRepository boardGameRepository, RegisteredUserRepository userRepository, BoardGameItemRepository boardGameItemRepository, BoardGameItemService boardGameItemService) {
        this.boardGameRepository = boardGameRepository;
        this.userRepository = userRepository;
        this.boardGameItemRepository = boardGameItemRepository;
        this.boardGameItemService = boardGameItemService;
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
    public void addGameToFavorites(RegisteredUser user, Long gameId) {
        BoardGame boardGameToAdd = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToAdd == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        if (user == null) {
            throw new ParametersException("User must not be null");
        }

        if (userRepository.findAllFavoriteGames(user.getId()).contains(boardGameToAdd.getName())) {
            throw new GameAlreadyInFavoritesException("Game with name " + boardGameToAdd.getName() + " already in favorites");
        }

        user.getFavoriteBoardGames().add(boardGameToAdd);
        userRepository.save(user);
    }

    @Transactional // Added @Transactional
    public void removeGameFromFavorites(RegisteredUser user, Long gameId) {
        BoardGame boardGameToRemove = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToRemove == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        if (user == null) {
            throw new ParametersException("User must not be null");
        }

        if (!userRepository.findAllFavoriteGames(user.getId()).contains(boardGameToRemove.getName())) {
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
}