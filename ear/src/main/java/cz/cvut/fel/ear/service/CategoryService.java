package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.CategoryRepository;
import cz.cvut.fel.ear.exception.BoardGameAlreadyInCategoryException;
import cz.cvut.fel.ear.exception.CategoryAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
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
    private BoardGameRepository boardGameRepository;

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
            throw new CategoryAlreadyExistsException("Category with name " + name + " already exists");
        }
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional
    public void addBoardGameToCategory(long gameId, long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category with id " + categoryId + " not found"));

        BoardGame boardGame = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("BoardGame with id " + gameId + " not found"));
        if (boardGame.getName() == null) {
            throw new EntityNotFoundException("Board game with name " + boardGame.getName() + " does not exist");
        }
        if (boardGame.getCategories() == null) { // Pro jistotu
            boardGame.setCategories(new ArrayList<>());
        }

        if (boardGame.getCategories().contains(category)) {
            throw new BoardGameAlreadyInCategoryException("Board game " + boardGame.getName() + " in Category " + category.getName() + " already exists");
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
                .orElseThrow(() -> new EntityNotFoundException("Category with id " + categoryId + " not found"));

        BoardGame boardGame = boardGameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("BoardGame with id " + gameId + " not found"));

        if (boardGame.getCategories() != null) {
            boardGame.getCategories().remove(category);
        }

        if (category.getBoardGames() != null) {
            category.getBoardGames().remove(boardGame);
        }

        boardGameRepository.save(boardGame);
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
