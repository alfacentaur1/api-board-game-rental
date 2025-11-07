package cz.cvut.fel.ear.service.interfaces;

import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Category;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;

public interface CategoryServiceI {

    /**
     * Transactional <br>
     * Adds a new category with the given name
     * @param name name of the category to add
     * @throws EntityAlreadyExistsException when category already exists
     */
    @Transactional
    void addNewCategory(String name);

    /**
     * Transactional <br>
     * Removes a category
     * @param category category to remove
     * @throws EntityNotFoundException when category does not exist
     */
    @Transactional
    void removeCategory(Category category);

    /**
     * Transactional <br>
     * Removes a category by its name
     * @param categoryName name of the category to remove
     * @throws EntityNotFoundException when category does not exist
     */
    @Transactional
    void removeCategory(String categoryName);

    /**
     * Transactional <br>
     * Adds a board game to a category
     * @param boardGame board game to add
     * @param category category to add the board game to
     * @throws EntityNotFoundException when category or game does not exist
     */
    @Transactional
    void addBoardGameToCategory(BoardGame boardGame, Category category);

    /**
     * Transactional <br>
     * Adds a board game to a category by their ids
     * @param gameId id of the board game to add
     * @param categoryName name of the category
     * @throws EntityNotFoundException when category or game does not exist
     */
    @Transactional
    void addBoardGameToCategory(long gameId, String categoryName);

    /**
     * Transactional <br>
     * Removes a board game from a category
     * @param boardGame board game to remove
     * @param category category to remove the board game from
     * @throws EntityNotFoundException when category or game does not exist
     */
    @Transactional
    void removeBoardGameFromCategory(BoardGame boardGame, Category category);

    /**
     * Transactional <br>
     * Removes a board game from a category by their ids
     * @param gameId id of the board game to remove
     * @param categoryName name of the category
     * @throws EntityNotFoundException when category or game does not exist
     */
    @Transactional
    void removeBoardGameFromCategory(long gameId, String categoryName);
}
