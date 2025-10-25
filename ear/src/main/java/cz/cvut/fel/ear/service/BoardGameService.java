package cz.cvut.fel.ear.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.GameAlreadyInFavoritesException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.Collections;
import java.util.List;

@Service
public class BoardGameService {
    private final BoardGameRepository boardGameRepository;
    private final RegisteredUserRepository userRepository;

    public BoardGameService(BoardGameRepository boardGameRepository, RegisteredUserRepository userRepository) {
        this.boardGameRepository = boardGameRepository;
        this.userRepository = userRepository;
    }

    public BoardGame getBoardGame(int gameId) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(gameId);
        if (boardGame == null) {
            throw new EntityNotFoundException("Board game not found");
        }
        return boardGame;
    }

    public List<BoardGame> getAllBoardGames() {
        List<BoardGame> boardGames = boardGameRepository.findAll();
        if (boardGames == Collections.EMPTY_LIST) {
            throw new EntityNotFoundException("There are no board games");
        }
        return boardGames;
    }

    @Transactional
    public int createBoardGame(String name, String description) {
        List<String> allBoardGameNames = boardGameRepository.getAllBoardGameNames();
        if (allBoardGameNames.contains(name)) {
            throw new EntityAlreadyExistsException("Board game with title " + name + " already exists");
        }
        BoardGame boardGameToCreate = new BoardGame();
        boardGameToCreate.setName(name);
        boardGameToCreate.setDescription(description);
        boardGameRepository.save(boardGameToCreate);
        return boardGameToCreate.getId();
    }

    public void removeBoardGame(int gameId) {
        BoardGame boardGameToRemove = boardGameRepository.getBoardGameById(gameId);

        if (boardGameToRemove == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");

        }
        boardGameRepository.delete(boardGameToRemove);
    }

    public void updateBoardGameDescription(int gameId, String newDescription) {
        BoardGame boardGameToUpdate = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToUpdate == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        boardGameToUpdate.setDescription(newDescription);
    }

    public void addGameToFavorites(RegisteredUser user, int gameId) {
        BoardGame boardGameToAdd = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToAdd == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        else if (userRepository.findAllFavoriteGames(gameId).contains(boardGameToAdd.getName()) ){
            throw new GameAlreadyInFavoritesException("Game with name " + boardGameToAdd.getName() + " already in favorites");
        }
       user.getFavoriteBoardGames().add(boardGameToAdd);
       userRepository.save(user);

    }

    public void removeGameFromFavorites(RegisteredUser user, int gameId) {
        BoardGame boardGameToRemove = boardGameRepository.getBoardGameById(gameId);
        if (boardGameToRemove == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        if(!userRepository.findAllFavoriteGames(gameId).contains(boardGameToRemove.getName())){
            throw new EntityNotFoundException("Game with name " + boardGameToRemove.getName() + " not in favorites");
        }
        user.getFavoriteBoardGames().remove(boardGameToRemove);
        userRepository.save(user);
    }

    public int boardGameAvalaibleItems(int gameId) {
        return 0;
    }

    public void viewBoardGameDetails(BoardGame boardGame) {
        if (boardGame == null) {
            throw new EntityNotFoundException("Board game not found");
        }
        System.out.println(boardGame.getName());
        System.out.println(boardGame.getDescription());
        System.out.println("Avalaible in stock: " + boardGameAvalaibleItems(boardGame.getId()));

    }




}
