package cz.cvut.fel.ear.service.interfaces;

import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import jakarta.transaction.Transactional;

import java.util.List;

public interface BoardGameItemServiceI {

    /**
     * Returns the number of available items in stock for a given board game
     *
     * @param gameId id of the board game to check
     * @return the number of available items in stock for given board game
     * @throws EntityNotFoundException when board game with given id does not exists
     */
    int getAvailableItemsInStockNumber(long gameId);

    /**
     * Returns the number of available items in stock for a given board game
     *
     * @param game game to check the available items in stock
     * @return the number of available items in stock for given board game
     */
    int getAvailableItemsInStockNumber(BoardGame game);

    /**
     * Returns all board game items for a given board game
     *
     * @param gameId id of the board game to get items
     * @return list of board game items belonging to the given board game
     * @throws EntityNotFoundException when board game with given id does not exists
     */
    List<BoardGameItem> getGameItemsForBoardGame(long gameId);

    /**
     * Returns all board game items for a given board game
     *
     * @param game board game to get items
     * @return list of board game items belonging to the given board game
     */
    List<BoardGameItem> getGameItemsForBoardGame(BoardGame game);

    /**
     * Returns all available items for a given board game
     *
     * @param gameId id of the board game to get available items
     * @return list of available board game items for given board game
     * @throws EntityNotFoundException when board game with given id does not exists
     */
    List<BoardGameItem> getAvailableGameItemsForBoardGame(long gameId);

    /**
     * Returns all available items for a given board game
     * @param boardGame board game to get available items
     * @return list of available board game items for given board game
     */
    List<BoardGameItem> getAvailableGameItemsForBoardGame(BoardGame boardGame);

    /**
     * Transactional <br>
     * Adds a new board game item for a given board game
     *
     * @param gameId id of the board game to add an item for
     * @param serialNumber unique serial number of the board game item
     * @param state initial state of the board game item
     * @return id of the newly created board game item
     * @throws EntityNotFoundException when board game with given id does not exists
     */
    @Transactional
    long addBoardGameItem(long gameId, String serialNumber, BoardGameState state);

    /**
     * Transactional <br>
     * Removes a board game item by its id
     *
     * @param gameItemId id of the board game item to remove
     * @throws EntityNotFoundException when board game item with given id does not exists
     */
    @Transactional
    void removeBoardGameItem(long gameItemId);

    /**
     * Transactional <br>
     * Removes a board game item
     *
     * @param gameItem board game item to remove
     * @throws EntityNotFoundException when provided board game item does not exists
     */
    @Transactional
    void removeBoardGameItem(BoardGameItem gameItem);

    /**
     * Transactional <br>
     * Updates the state of a board game item by board game id
     *
     * @param itemId id of the board game item whose item state should be updated
     * @param newState new state to set for the board game item
     * @throws EntityNotFoundException when board game or item with given id does not exists
     */
    @Transactional
    void updateBoardGameItemState(long itemId, BoardGameState newState);

    /**
     * Transactional <br>
     * Updates the state of a board game item
     *
     * @param gameItem board game item whose item state should be updated
     * @param newState new state to set for the board game item
     * @throws EntityNotFoundException when provided board game does not exists
     */
    @Transactional
    void updateBoardGameItemState(BoardGameItem gameItem, BoardGameState newState);

    /**
     * Returns all currently borrowed board game items
     * @return list of all currently borrowed board game items
     */
    List<BoardGameItem> getCurrentlyBorrowedItems();
}
