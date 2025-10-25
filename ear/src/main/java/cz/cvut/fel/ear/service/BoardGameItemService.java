package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BoardGameItemService {
    private final BoardGameItemRepository boardGameItemRepository;
    private final BoardGameRepository boardGameRepository;

    public BoardGameItemService(BoardGameItemRepository boardGameItemRepository, BoardGameRepository boardGameRepository) {
        this.boardGameItemRepository = boardGameItemRepository;
        this.boardGameRepository = boardGameRepository;
    }

    public int avalaibleItemsInStockNumber(long boardGameId) {
        BoardGame boardGame = boardGameRepository.findById(boardGameId).get();
        if (boardGame.getName() == null) {
            throw new EntityNotFoundException("Board game with id " + boardGameId + " not found");
        }
        return getAllAvalableBoardGameItemsForBoardGame(boardGameId).size();
    }

    public List<BoardGameItem> getAllBoardGameItemsForBoardGame(long boardGameId) {
        BoardGame boardGame = boardGameRepository.findBoardGameById(boardGameId);

        if (boardGame == null) {
            throw new EntityNotFoundException("Board game with id " + boardGameId + " not found");
        }
        return boardGame.getAvailableStockItems();
    }

    public List<BoardGameItem> getAllAvalableBoardGameItemsForBoardGame(long boardGameId) {
        BoardGame boardGame = boardGameRepository.findById(boardGameId).get();
        if(boardGame.getName() == null) {
            throw new EntityNotFoundException("Board game with id " + boardGameId + " not found");
        }

        List<BoardGameItem> boardGameItems = getAllBoardGameItemsForBoardGame(boardGameId);
        List<BoardGameItem> boardGameAvailableItems = new ArrayList<>();;
        for (BoardGameItem boardGameItem : boardGameItems) {
            if(boardGameItem.getState() != BoardGameState.NOT_FOR_LOAN && boardGameItem.getState() != BoardGameState.BORROWED){
                boardGameAvailableItems.add(boardGameItem);
            }
    }
        return boardGameAvailableItems;
    }


    public long addBoardGameItem(long boardGameId, String serialNumber, BoardGameState state) {
        BoardGame boardGame = boardGameRepository.findBoardGameById(boardGameId);

        if (boardGame == null) {
            throw new EntityNotFoundException("Board game with id " + boardGameId + " not found");
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
        return boardGameItemRepository.save(boardGameItem).getId();
    }


    public void updateBoardGameItemState(long gameId, BoardGameState state) {
        BoardGameItem boardGameToUpdate = boardGameItemRepository.getBoardGameItemById(gameId);
        if (boardGameToUpdate == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
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



}
