package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.CategoryRepository;
import cz.cvut.fel.ear.exception.BoardGameAlreadyInCategoryException;
import cz.cvut.fel.ear.exception.CategoryAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.ItemNotInResource;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Category;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BoardGameRepository boardGameRepository;

    public CategoryService(CategoryRepository categoryRepository, BoardGameRepository boardGameRepository) {
        this.categoryRepository = categoryRepository;
        this.boardGameRepository = boardGameRepository;
    }

    public long addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty.");
        }
        Optional<Category> existing = categoryRepository.findByName(name);
        if (existing.isPresent()) {
            throw new CategoryAlreadyExistsException(name);
        }
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional
    public void addBoardGameToCategory(long gameId, long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(Category.class.getSimpleName(), categoryId));

        BoardGame boardGame = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException(BoardGame.class.getSimpleName(), gameId));
        if (boardGame.getName() == null) {
            throw new EntityNotFoundException(BoardGame.class.getSimpleName(), gameId);
        }
        if (boardGame.getCategories() == null) {
            boardGame.setCategories(new ArrayList<>());
        }

        if (boardGame.getCategories().contains(category)) {
            throw new BoardGameAlreadyInCategoryException();
        }

        boardGame.getCategories().add(category);

        if (category.getBoardGames() == null) {
            category.setBoardGames(new ArrayList<>());
        }
        category.getBoardGames().add(boardGame);

        boardGameRepository.save(boardGame);

    }

    @Transactional
    public void removeGameFromCategory(long gameId, long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(Category.class.getSimpleName(),categoryId));

        BoardGame boardGame = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException(BoardGame.class.getSimpleName(),gameId));

        // Check if boardGame is in Category
        if (
                boardGame.getCategories().contains(category) && category.getBoardGames().contains(boardGame)
        ) {
            boardGame.getCategories().remove(category);
            category.getBoardGames().remove(boardGame);

            boardGameRepository.save(boardGame);
            categoryRepository.save(category);
        } else {
            throw new ItemNotInResource("BoardGame", "Category");
        }
    }

    public List<Long> getCategories(long gameId) {
        List<Long> categories = new ArrayList<>();
        BoardGame boardGame = boardGameRepository.findBoardGameById(gameId);
        List<Category> categoriesList = boardGame.getCategories();
        for (Category category : categoriesList) {
            categories.add(category.getId());
        }
        return categories;
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }





}
