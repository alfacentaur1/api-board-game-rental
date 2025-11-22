package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.exception.CategoryAlreadyExistsException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.Category;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.CategoryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model")
@ActiveProfiles("test")
public class CategoryServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BoardGameRepository boardGameRepository;

    private BoardGame testGame;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testGame = new BoardGame("Catan");
        testCategory = new Category();
        testCategory.setName("Strategy");
        em.persist(testGame);
        em.persist(testCategory);
        em.flush();
    }

    @Test
    @DisplayName("Should create and persist a new category")
    void addCategory_createsAndPersistsCategory() {
        // Act
        String categoryName = "Family Game";
        long newCategoryId = categoryService.addCategory(categoryName);

        // Assert
        Category found = em.find(Category.class, newCategoryId);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("Should associate a game with a category on both sides")
    void addBoardGameToCategory_associatesGameAndCategoryCorrectly() {
        // Act
        categoryService.addBoardGameToCategory(testGame.getId(), testCategory.getId());

        // Assert
        em.flush();
        em.clear();

        BoardGame updatedGame = boardGameRepository.findById(testGame.getId()).orElse(null);
        Category updatedCategory = categoryRepository.findById(testCategory.getId()).orElse(null);

        assertThat(updatedGame).isNotNull();
        assertThat(updatedCategory).isNotNull();

        //validate both sides
        assertThat(updatedGame.getCategories()).contains(updatedCategory);

        assertThat(updatedCategory.getBoardGames()).contains(updatedGame);
    }

    @Test
    @DisplayName("Should disassociate a game from a category on both sides")
    void removeGameFromCategory_disassociatesGameAndCategory() {
        // Arrange
        categoryService.addBoardGameToCategory(testGame.getId(), testCategory.getId());
        em.flush();
        em.clear();

        BoardGame gameBeforeRemove = boardGameRepository.findById(testGame.getId()).get();
        assertThat(gameBeforeRemove.getCategories()).isNotEmpty();

        // Act
        categoryService.removeGameFromCategory(testGame.getId(), testCategory.getId());
        em.flush();
        em.clear();

        // Assert
        BoardGame updatedGame = boardGameRepository.findById(testGame.getId()).orElse(null);
        Category updatedCategory = categoryRepository.findById(testCategory.getId()).orElse(null);

        assertThat(updatedGame).isNotNull();
        assertThat(updatedCategory).isNotNull();

        // verify both sides
        assertThat(updatedGame.getCategories()).doesNotContain(updatedCategory);
        assertThat(updatedCategory.getBoardGames()).doesNotContain(updatedGame);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when game does not exist")
    void addBoardGameToCategory_throwsException_whenGameNotFound() {
        // Act & Assert
        long nonExistentGameId = -99L;

        // verify exception
        assertThrows(cz.cvut.fel.ear.exception.EntityNotFoundException.class, () -> {
            categoryService.addBoardGameToCategory(nonExistentGameId, testCategory.getId());
        });
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when category does not exist")
    void addBoardGameToCategory_throwsException_whenCategoryNotFound() {
        // Act & Assert
        long nonExistentCategoryId = -99L;

        assertThrows(cz.cvut.fel.ear.exception.EntityNotFoundException.class, () -> {
            categoryService.addBoardGameToCategory(testGame.getId(), nonExistentCategoryId);
        });
    }


    @Test
    @DisplayName("Should throw CategoryAlreadyExistsException when name is duplicate")
    void addCategory_throwsException_whenNameIsDuplicate() {
        // Arrange
        // "Strategy" was already persisted in setUp()
        String existingName = "Strategy";

        // Act & Assert
        assertThrows(CategoryAlreadyExistsException.class, () -> {
            categoryService.addCategory(existingName);
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is null")
    void addCategory_throwsException_whenNameIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.addCategory(null);
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is empty")
    void addCategory_throwsException_whenNameIsEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.addCategory("");
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when name is only whitespace")
    void addCategory_throwsException_whenNameIsWhitespace() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.addCategory("   ");
        });
    }

    @Test
    @DisplayName("Should successfully add case-sensitive names")
    void addCategory_allowsCaseSensitiveNames() {
        // Arrange
        // "Strategy" (uppercase S) exists from setUp()
        String newName = "strategy"; // lowercase s

        // Act
        long newCategoryId = categoryService.addCategory(newName);

        // Assert
        Category found = em.find(Category.class, newCategoryId);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(newName);

        // Verify the original still exists and is different
        Category original = categoryRepository.findByName("Strategy").orElse(null);
        assertThat(original).isNotNull();
        assertThat(original.getId()).isNotEqualTo(newCategoryId);
    }
}