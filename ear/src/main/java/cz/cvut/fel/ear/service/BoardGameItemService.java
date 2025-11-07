package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import cz.cvut.fel.ear.service.interfaces.BoardGameItemServiceI;

import java.util.ArrayList;
import java.util.List;

public class BoardGameItemService implements BoardGameItemServiceI {
    private final BoardGameRepository gameRepository;
    private final BoardGameItemRepository gameItemRepository;

    public BoardGameItemService(BoardGameRepository gameRepository, BoardGameItemRepository gameItemRepository) {
        this.gameRepository = gameRepository;
        this.gameItemRepository = gameItemRepository;
    }

    @Override
    public int gatAvailableItemsInStockNumber(long gameId) {
        // Check if game exists
        BoardGame game = findBoardGame(gameId);

        // Return the amount of available board game items
        return getAvailableGameItemsForBoardGame(game.getId()).size();
    }

    @Override
    public int gatAvailableItemsInStockNumber(BoardGame game) {
        // Return the amount of available board game items
        return getAvailableGameItemsForBoardGame(game.getId()).size();
    }

    @Override
    public List<BoardGameItem> getGameItemsForBoardGame(long gameId) {
        // Find the board game
        BoardGame boardGame = findBoardGame(gameId);

        // Return the list of board game items
        return boardGame.getAvailableStockItems();
    }

    @Override
    public List<BoardGameItem> getGameItemsForBoardGame(BoardGame game) {
        return  game.getAvailableStockItems();
    }

    @Override
    public List<BoardGameItem> getAvailableGameItemsForBoardGame(long gameId) {
        // Find the board game
        BoardGame game = findBoardGame(gameId);

        return filterAvailableItems(game.getId());

    }

    @Override
    public List<BoardGameItem> getAvailableGameItemsForBoardGame(BoardGame boardGame) {
        return filterAvailableItems(boardGame.getId());
    }

    @Override
    public long addBoardGameItem(long gameId, String serialNumber, BoardGameState state) {
        // Check if game exists
        BoardGame game = findBoardGame(gameId);

        // Create new board game item
        BoardGameItem newItem = new BoardGameItem();
        newItem.setBoardGame(game);
        newItem.setSerialNumber(serialNumber);
        newItem.setState(state);

        return gameItemRepository.save(newItem).getId();
    }

    @Override
    public void removeBoardGameItem(long gameItemId) {
        // Find board game item
        BoardGameItem gameItem = findBoardGameItem(gameItemId);

        // Remove it
        gameItemRepository.delete(gameItem);

    }

    @Override
    public void removeBoardGameItem(BoardGameItem gameItem) {
        // Remove it
        gameItemRepository.delete(gameItem);
    }

    @Override
    public void updateBoardGameItemState(long itemId, BoardGameState newState) {
        // Find board game item
        BoardGameItem gameItem = findBoardGameItem(itemId);

        // update the state
        updateState(gameItem, newState);
    }

    @Override
    public void updateBoardGameItemState(BoardGameItem gameItem, BoardGameState newState) {
        updateState(gameItem, newState);
    }

    @Override
    public List<BoardGameItem> getCurrentlyBorrowedItems() {
        // Get all game Items
        List<BoardGameItem> allItems = gameItemRepository.findAll();

        // Filter currently borrowed
        List<BoardGameItem> borrowedItems = new ArrayList<>();
        for (BoardGameItem gameItem : allItems) {
            if (gameItem.getState() == BoardGameState.BORROWED) {
                borrowedItems.add(gameItem);
            }
        }

        return borrowedItems;
    }

    /**
     * Finds a board game by its id
     *
     * @param gameId id of the board game to find
     * @return the board game with the given id
     * @throws EntityNotFoundException when board game with given id does not exists
     */
    private BoardGame findBoardGame(long gameId) {
        BoardGame boardGame = gameRepository.findBoardGameById(gameId);

        // Check if boardGame was found
        if (boardGame == null) {
            throw new EntityNotFoundException(
                    String.format("Board game with id %d not found", boardGame.getId())
            );
        }
        return boardGame;
    }

    /**
     * Finds a board game item by its id
     *
     * @param itemId id of the board game item to find
     * @return the board game item with the given id
     * @throws EntityNotFoundException when board game item with given id does not exists
     */
    private BoardGameItem findBoardGameItem(long itemId) {
        BoardGameItem gameItem = gameItemRepository.getBoardGameItemById(itemId);

        // Check if the item was found
        if (gameItem == null) {
            throw new EntityNotFoundException(
                    String.format("Board game item with id %d not found", itemId)
            );
        }
        return gameItem;
    }

    /**
     * Filters the available items for a given board game
     *
     * @param gameId id of the board game to filter items for
     * @return list of available board game items for the given board game
     */
    private List<BoardGameItem> filterAvailableItems(long gameId) {
        // Get all board game items
        List<BoardGameItem> allItems = getGameItemsForBoardGame(gameId);
        List<BoardGameItem> availableItems = new ArrayList<>();

        // Filter available items
        for (BoardGameItem gameItem : allItems) {
            if (gameItem.getState() == BoardGameState.FOR_LOAN) {
                availableItems.add(gameItem);
            }
        }

        return availableItems;
    }

    /**
     * Updates the state of a board game item
     *
     * @param gameItem board game item to update
     * @param newState new state to set for the board game item
     */
    private void updateState(BoardGameItem gameItem, BoardGameState newState) {
        // Update the state
        gameItem.setState(newState);

        // Save the state
        gameItemRepository.save(gameItem);
    }
}
