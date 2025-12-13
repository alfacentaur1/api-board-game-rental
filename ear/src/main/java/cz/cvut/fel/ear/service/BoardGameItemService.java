package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
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
    private final BoardGameLoanRepository boardGameLoanRepository;

    public BoardGameItemService(BoardGameItemRepository boardGameItemRepository, BoardGameRepository boardGameRepository, BoardGameLoanRepository boardGameLoanRepository) {
        this.boardGameItemRepository = boardGameItemRepository;
        this.boardGameRepository = boardGameRepository;
        this.boardGameLoanRepository = boardGameLoanRepository;
    }

    /**
     * Calculates the number of available items in stock for a specific board game.
     *
     * @param boardGameId the ID of the board game
     * @return the count of available items
     */
    public int availableItemsInStockNumber(long boardGameId) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(boardGameId);
        return getAllAvailableBoardGameItemsForBoardGame(boardGameId).size();
    }

    /**
     * Retrieves all items (regardless of state) associated with a board game.
     *
     * @param boardGameId the ID of the board game
     * @return a list of all board game items
     * @throws EntityNotFoundException if the board game is not found
     */
    public List<BoardGameItem> getAllBoardGameItemsForBoardGame(long boardGameId) {
        BoardGame boardGame = boardGameRepository.findBoardGameById(boardGameId);

        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), boardGameId);
        }
        return boardGame.getAvailableStockItems();
    }

    /**
     * Retrieves all items for a board game that are available for loan.
     *
     * @param boardGameId the ID of the board game
     * @return a list of available board game items
     * @throws EntityNotFoundException if the board game is not found
     */
    public List<BoardGameItem> getAllAvailableBoardGameItemsForBoardGame(long boardGameId) {
        Optional<BoardGame> optionalBoardGame = boardGameRepository.findById(boardGameId);

        if (optionalBoardGame.isEmpty()) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), boardGameId);
        }

        List<BoardGameItem> boardGameItems = getAllBoardGameItemsForBoardGame(boardGameId);
        List<BoardGameItem> boardGameAvailableItems = new ArrayList<>();
        for (BoardGameItem boardGameItem : boardGameItems) {
            if (boardGameItem.getState() != BoardGameState.NOT_FOR_LOAN && boardGameItem.getState() != BoardGameState.BORROWED) {
                boardGameAvailableItems.add(boardGameItem);
            }
        }
        return boardGameAvailableItems;
    }

    /**
     * Creates a new item for a specific board game.
     *
     * @param boardGameId  the ID of the board game
     * @param serialNumber the serial number of the new item
     * @param state        the initial state of the item
     * @return the ID of the created item
     * @throws EntityNotFoundException if the board game is not found
     * @throws ParametersException     if parameters are invalid
     */
    @Transactional
    public long addBoardGameItem(long boardGameId, String serialNumber, BoardGameState state) {
        BoardGame boardGame = boardGameRepository.findBoardGameById(boardGameId);

        if (boardGame == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), boardGameId);
        }
        if (serialNumber == null || serialNumber.isEmpty() || state == null) {
            throw new ParametersException("Serial number or state is null");
        }
        try {
            BoardGameState.valueOf(state.toString());
        } catch (IllegalArgumentException e) {
            throw new ParametersException("State is not valid");
        }
        // Check if item with this serialNumber exists
        List<BoardGameItem> allItems = getAllBoardGameItemsForBoardGame(boardGameId);
        boolean serialNumberExists = allItems.stream()
                .anyMatch(item -> item.getSerialNumber().equals(serialNumber));

        if (serialNumberExists) {
            throw new EntityAlreadyExistsException("BoardGameItem", boardGameId);
        }

        BoardGameItem boardGameItem = new BoardGameItem();
        boardGameItem.setBoardGame(boardGame);
        boardGameItem.setSerialNumber(serialNumber);
        boardGameItem.setState(state);

        boardGame.getAvailableStockItems().add(boardGameItem);
        return boardGameItemRepository.save(boardGameItem).getId();
    }

    /**
     * Updates the state of an existing board game item.
     *
     * @param itemId the ID of the item
     * @param state  the new state
     * @throws EntityNotFoundException if the item is not found
     */
    public void updateBoardGameItemState(long itemId, BoardGameState state) {
        BoardGameItem itemToUpdate = boardGameItemRepository.getBoardGameItemById(itemId);

        if (itemToUpdate == null) {
            throw new EntityNotFoundException("BoardGameItem", itemId);
        }

        itemToUpdate.setState(state);
        boardGameItemRepository.save(itemToUpdate);
    }

    /**
     * Deletes a board game item. If the item has been used in loan history, it is dissociated from the board game
     * and its state is set to NOT_FOR_LOAN. Otherwise, it is hard deleted.
     *
     * @param itemId the ID of the item to delete
     * @throws EntityNotFoundException if the item is not found
     */
    @Transactional
    public void deleteBoardGameItem(long itemId) {
        BoardGameItem item = boardGameItemRepository.getBoardGameItemById(itemId);
        if (item == null) {
            throw new EntityNotFoundException(BoardGameItem.class.getSimpleName(), itemId);
        }
        // Check if the item has been used in any loan history
        boolean isUsedInHistory = boardGameLoanRepository.countLoansWithItem(itemId) > 0;

        // If it has been used, we cannot delete it completely
        // Instead, we dissociate it from the board game and set its state to NOT_FOR_LOAN
        if (isUsedInHistory) {
            BoardGame parent = item.getBoardGame();
            if (parent != null) {
                parent.getAvailableStockItems().remove(item);
                item.setCachedGameName(parent.getName());
            }
            item.setBoardGame(null);
            item.setState(BoardGameState.NOT_FOR_LOAN);

            boardGameItemRepository.save(item);

        }
        // If it hasn't been used, we can safely delete it - HARD DELETE
        else {
            BoardGame parent = item.getBoardGame();
            if (parent != null) {
                parent.getAvailableStockItems().remove(item);
            }
            boardGameItemRepository.delete(item);
        }
    }
}