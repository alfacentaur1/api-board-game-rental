package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.CategoryRepository;
import cz.cvut.fel.ear.exception.BoardGameAlreadyInCategoryException;
import cz.cvut.fel.ear.exception.CategoryAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Category;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private CategoryRepository categoryRepository;
    private BoardGameRepository boardGameRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public long addCategory(String name) {
        if(categoryRepository.findAll().contains(name)){
            throw new CategoryAlreadyExistsException("Category with name " + name + " already exists");
        }
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return category.getId();
    }


    public void addBoardGameToCategory(long gameId, long categoryId) {
        Category category = categoryRepository.findById(categoryId).get();
        BoardGame boardGame = boardGameRepository.findById(gameId).get();
        if(category.getBoardGames().contains(boardGame)){
            throw new BoardGameAlreadyInCategoryException("Board game " + boardGame.getName() + " in Category " + category.getName() + " already exists");
        }
        if(category.getName() == null){
            throw new EntityNotFoundException("Category with name " + category.getName() + " does not exist");
        }
        if(boardGame.getName() == null){
            throw new EntityNotFoundException("Board game with name " + boardGame.getName() + " does not exist");
        }
        category.getBoardGames().add(boardGame);
        categoryRepository.save(category);

    }

    public void removeGameFromCategory(long gameId, long categoryId) {
        Category category = categoryRepository.findById(categoryId).get();
        BoardGame boardGame = boardGameRepository.findById(gameId).get();
        if(category.getName() == null){
            throw new EntityNotFoundException("Category with id " + categoryId + " does not exist");
        }
        else if(boardGame.getName() == null){
            throw new EntityNotFoundException("Board game with id " + gameId + " does not exist");
        }
        category.getBoardGames().remove(boardGame);
        categoryRepository.save(category);
    }


}
