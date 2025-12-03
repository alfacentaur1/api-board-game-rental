package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BoardGameItemService {
    private final BoardGameItemRepository boardGameItemRepository;
    private final BoardGameRepository boardGameRepository;

    public BoardGameItemService(BoardGameItemRepository boardGameItemRepository, BoardGameRepository boardGameRepository) {
        this.boardGameItemRepository = boardGameItemRepository;
        this.boardGameRepository = boardGameRepository;
    }

    public int availableItemsInStockNumber(long boardGameId) {
        BoardGame boardGame = boardGameRepository.findById(boardGameId).get();
        if (boardGame.getName() == null) {
            throw new EntityNotFoundException(boardGame.getClass().getSimpleName(), boardGameId);
        }
        return getAllAvailableBoardGameItemsForBoardGame(boardGameId).size();
    }

    public List<BoardGameItem> getAllBoardGameItemsForBoardGame(long boardGameId) {
        BoardGame boardGame = boardGameRepository.findBoardGameById(boardGameId);

        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), boardGameId);
        }
        return boardGame.getAvailableStockItems();
    }

    public List<BoardGameItem> getAllAvailableBoardGameItemsForBoardGame(long boardGameId) {
        Optional<BoardGame> optionalBoardGame = boardGameRepository.findById(boardGameId);

        if (optionalBoardGame.isEmpty()) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(),boardGameId);
        }

        List<BoardGameItem> boardGameItems = getAllBoardGameItemsForBoardGame(boardGameId);
        List<BoardGameItem> boardGameAvailableItems = new ArrayList<>();
        for (BoardGameItem boardGameItem : boardGameItems) {
            if(boardGameItem.getState() != BoardGameState.NOT_FOR_LOAN && boardGameItem.getState() != BoardGameState.BORROWED){
                boardGameAvailableItems.add(boardGameItem);
            }
    }
        return boardGameAvailableItems;
    }

    @Transactional
    public long addBoardGameItem(long boardGameId, String serialNumber, BoardGameState state) {
        BoardGame boardGame = boardGameRepository.findBoardGameById(boardGameId);

        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(),boardGameId);
        }
        if( serialNumber == null || serialNumber.isEmpty() || state == null){
            throw new ParametersException("Serial number or state is null");
        }
        try {
            BoardGameState stateToTest = BoardGameState.valueOf(state.toString());
        } catch (IllegalArgumentException e) {
            throw new ParametersException("State is not valid");
        }
        BoardGameItem boardGameItem = new BoardGameItem();
        boardGameItem.setBoardGame(boardGame);
        boardGameItem.setSerialNumber(serialNumber);
        boardGameItem.setState(state);
        //fix - bidirectional approach
        boardGame.getAvailableStockItems().add(boardGameItem);
        return boardGameItemRepository.save(boardGameItem).getId();
    }


    public void updateBoardGameItemState(long gameId, BoardGameState state) {
        if (gameId <= 0) {
            throw new ParametersException("Game id must be greater than 0");
        }
        BoardGameItem boardGameToUpdate = boardGameItemRepository.getBoardGameItemById(gameId);
        if (boardGameToUpdate == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(),gameId);
        }
        Long gameNull = null;
        if(gameId == gameNull ){
            throw new ParametersException("Game id is null");
        }
        if(state == null){
            throw new ParametersException("State is null");
        }
        try {
            BoardGameState stateToTest = BoardGameState.valueOf(state.toString());
        } catch (IllegalArgumentException e) {
            throw new ParametersException("State is not valid");
        }
        boardGameToUpdate.setState(state);
        boardGameItemRepository.save(boardGameToUpdate);

    }

    public void deleteBoardGameItem(long gameId) {
        BoardGameItem boardGameItemToDelete = boardGameItemRepository.getBoardGameItemById(gameId);
        if (boardGameItemToDelete == null) {
            throw new EntityNotFoundException(BoardGameItemService.class.getSimpleName(),gameId);
        }
        boardGameItemRepository.delete(boardGameItemToDelete);
    }



}
