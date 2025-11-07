package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.CategoryRepository;
import cz.cvut.fel.ear.exception.BoardGameAlreadyInCategoryException;
import cz.cvut.fel.ear.exception.BoardGameNotFoundInCategory;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Category;
import cz.cvut.fel.ear.service.interfaces.CategoryServiceI;

public class CategoryService implements CategoryServiceI {
    private final CategoryRepository categoryRepository;
    private final BoardGameService gameService;


    public CategoryService(CategoryRepository categoryRepository, BoardGameService gameService) {
        this.categoryRepository = categoryRepository;
        this.gameService = gameService;
    }

    @Override
    public void addNewCategory(String name) {
        if (categoryExists(name)) {
            throw new EntityAlreadyExistsException(
                    String.format("Category with name %s already exists", name)
            );
        }

        // Create new category
        Category category = new Category();
        category.setName(name);

        // Save it
        categoryRepository.save(category);
    }

    @Override
    public void removeCategory(Category category) {
        categoryRepository.delete(category);

    }

    @Override
    public void removeCategory(String categoryName) {
        // Check if category with given name exists
        if (
                !categoryExists(categoryName)
        ) {
            throw new EntityNotFoundException(
                    String.format("Category with name %s not found", categoryName)
            );
        }

        // Find the category
        Category category = findCategory(categoryName);

        categoryRepository.delete(category);

    }

    @Override
    public void addBoardGameToCategory(BoardGame boardGame, Category category) {
        addGameToCategory(boardGame, category);

    }

    public void addBoardGameToCategory(long gameId, String categoryName) {
        // Find the boardGame
        BoardGame game = gameService.getBoardGame(gameId);

        // Find the category
        Category category = findCategory(categoryName);

        // Add the game to the category
        addGameToCategory(game, category);
    }

    @Override
    public void removeBoardGameFromCategory(BoardGame boardGame, Category category) {
        removeGameFromCategory(boardGame, category);
    }

    @Override
    public void removeBoardGameFromCategory(long gameId, String categoryName) {
        // Find the game and category
        BoardGame game = gameService.getBoardGame(gameId);
        Category category = findCategory(categoryName);

        removeGameFromCategory(game, category);
    }

    private boolean categoryExists(String categoryName) {
        return categoryRepository.findAll().contains(categoryName);
    }

    private Category findCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName).get();

        if (category == null) {
            throw new EntityNotFoundException(
                    String.format("Category with name %s not found", categoryName)
            );
        }
        return category;
    }

    private void addGameToCategory(BoardGame game, Category category) {
        // Check if game is already in category
        if (category.getBoardGames().contains(game)) {
            throw new BoardGameAlreadyInCategoryException(
                    String.format("Board game %s is already in category %s", game.getName(), category.getName())
            );
        }

        category.getBoardGames().add(game);
        categoryRepository.save(category);
    }

    private void removeGameFromCategory(BoardGame game, Category category) {
        // Check if game is in the category
        if (category.getBoardGames().contains(game)) {
            throw new BoardGameNotFoundInCategory(
                    String.format("Board game %s is not in the category %s", game.getName(), category.getName())
            );
        }

        // Remove it
        category.getBoardGames().remove(game);
        categoryRepository.save(category);
    }
}
