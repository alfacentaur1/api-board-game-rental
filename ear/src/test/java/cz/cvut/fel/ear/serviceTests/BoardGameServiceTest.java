package cz.cvut.fel.ear.serviceTests;

// Import models, DAOs, and services
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.service.BoardGameService;
import cz.cvut.fel.ear.exception.*; // All your custom exceptions

// Imports for testing
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Use TestEntityManager
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan; // Import ComponentScan
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.dao.DataIntegrityViolationException; // For checking DB constraints

import java.util.ArrayList;
import java.util.List;

// Imports for assertions
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// --- Annotations from your CategoryServiceTest ---
@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model") // Scan for services and DAOs
@ActiveProfiles("test")
public class BoardGameServiceTest {

    // --- We inject real beans, not mocks ---
    @Autowired
    private TestEntityManager em; // To prepare data in the database

    @Autowired
    private BoardGameService sut; // The real service bean

    @Autowired
    private BoardGameRepository boardGameRepo; // The real repository bean

    @Autowired
    private RegisteredUserRepository userRepo; // The real repository bean

    // --- Test Data Entities ---
    private RegisteredUser testUser;
    private BoardGame testGame1_Catan;
    private BoardGame testGame2_Wingspan;
    private long catanId;
    private long wingspanId;
    private long userId;

    @BeforeEach
    void setUp() {
        // We use TestEntityManager to set up the DB state

        // 1. Create a user
        testUser = new RegisteredUser();
        // (Assuming you have setters for username, karma, etc.)
        // testUser.setUsername("tester");
        em.persist(testUser);

        // 2. Create game 1 (Catan)
        testGame1_Catan = new BoardGame("Catan");
        testGame1_Catan.setDescription("Trade sheep for wood.");
        em.persist(testGame1_Catan);

        // 3. Create game 2 (Wingspan)
        testGame2_Wingspan = new BoardGame("Wingspan");
        testGame2_Wingspan.setDescription("A game about birds.");
        em.persist(testGame2_Wingspan);

        // 4. Link 'Wingspan' as a favorite to the user
        // (Assumes RegisteredUser is the owner of the relationship)
        if (testUser.getFavoriteBoardGames() == null) {
            testUser.setFavoriteBoardGames(new ArrayList<>());
        }
        testUser.getFavoriteBoardGames().add(testGame2_Wingspan);
        em.merge(testUser); // Save the owner (User)

        em.flush(); // Write all changes to the DB

        // Store IDs for tests
        catanId = testGame1_Catan.getId();
        wingspanId = testGame2_Wingspan.getId();
        userId = testUser.getId();

        em.clear(); // Clear the cache, so tests read fresh data
    }

    // --- Tests for getBoardGame ---

    @Test
    @DisplayName("getBoardGame() should return game when exists")
    public void getBoardGame_shouldReturnGame_whenExists() {
        // Act
        BoardGame found = sut.getBoardGame(catanId);
        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Catan");
    }

    @Test
    @DisplayName("getBoardGame() should throw EntityNotFoundException when game not found")
    public void getBoardGame_shouldThrowException_whenNotFound() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            sut.getBoardGame(-99L);
        });
    }

    // --- Tests for getAllBoardGames ---

    @Test
    @DisplayName("getAllBoardGames() should return list of games")
    public void getAllBoardGames_shouldReturnList() {
        // Act
        List<BoardGame> games = sut.getAllBoardGames();
        // Assert
        assertThat(games).isNotNull().hasSize(2);
        assertThat(games).extracting(BoardGame::getName).contains("Catan", "Wingspan");
    }

    @Test
    @DisplayName("getAllBoardGames() should throw EntityNotFoundException when list is empty")
    public void getAllBoardGames_shouldThrowException_whenEmpty() {
        // Arrange
        RegisteredUser user = em.find(RegisteredUser.class, userId);

        user.getFavoriteBoardGames().clear();
        em.merge(user);
        em.flush();

        boardGameRepo.deleteAll();
        em.flush();

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            sut.getAllBoardGames();
        });
    }

    // --- Tests for createBoardGame ---

    @Test
    @DisplayName("createBoardGame() should successfully create a game")
    public void createBoardGame_shouldCreateGame_whenValid() {
        // Act
        long newId = sut.createBoardGame("New Game", "Description");
        em.flush(); // Make sure the save is executed

        // Assert
        BoardGame found = em.find(BoardGame.class, newId);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("New Game");
    }

    @Test
    @DisplayName("createBoardGame() should throw EntityAlreadyExistsException for duplicate")
    public void createBoardGame_shouldThrowException_whenDuplicate() {
        // Act & Assert
        assertThrows(EntityAlreadyExistsException.class, () -> {
            sut.createBoardGame("Catan", "Another description");
        });
    }

    @Test
    @DisplayName("createBoardGame() should throw ParametersException for empty name")
    public void createBoardGame_shouldThrowException_whenNameIsEmpty() {
        // Act & Assert
        assertThrows(ParametersException.class, () -> {
            sut.createBoardGame("", "Description");
        });
    }

    // --- Tests for removeBoardGame ---

    @Test
    @DisplayName("removeBoardGame() should delete game when exists")
    public void removeBoardGame_shouldCallDelete_whenExists() {
        // Act
        sut.removeBoardGame(catanId);
        em.flush(); // Ensure delete is committed

        // Assert
        BoardGame found = em.find(BoardGame.class, catanId);
        assertThat(found).isNull();
    }

    // --- Tests for updateBoardGameDescription ---

    @Test
    @DisplayName("updateBoardGameDescription() should save the new description")
    public void updateBoardGameDescription_shouldChangeDescription() {
        // Arrange
        String newDescription = "New Description";

        // Act
        sut.updateBoardGameDescription(catanId, newDescription);
        em.flush(); // Commit the transaction
        em.clear(); // Clear cache to force a reload

        // Assert
        BoardGame updated = em.find(BoardGame.class, catanId);
        // This test will FAIL if your service is missing 'boardGameRepository.save()'
        assertThat(updated.getDescription()).isEqualTo(newDescription);
    }

    // --- Tests for addGameToFavorites ---

    @Test
    @DisplayName("addGameToFavorites() should add game and save user")
    public void addGameToFavorites_shouldAddGameAndSaveUser() {
        // Arrange
        RegisteredUser user = em.find(RegisteredUser.class, userId);

        // Act
        sut.addGameToFavorites(user, catanId); // Add Catan (not a favorite yet)
        em.flush();
        em.clear();

        // Assert
        RegisteredUser updatedUser = em.find(RegisteredUser.class, userId);
        assertThat(updatedUser.getFavoriteBoardGames()).hasSize(2);
        assertThat(updatedUser.getFavoriteBoardGames()).extracting(BoardGame::getName).contains("Catan", "Wingspan");
    }

    @Test
    @DisplayName("addGameToFavorites() should throw GameAlreadyInFavoritesException for duplicate")
    public void addGameToFavorites_shouldThrowException_whenDuplicate() {
        // Arrange
        RegisteredUser user = em.find(RegisteredUser.class, userId);

        // Act & Assert
        // Wingspan was added in setUp()
        assertThrows(GameAlreadyInFavoritesException.class, () -> {
            sut.addGameToFavorites(user, wingspanId);
        });
    }

    // --- Tests for removeGameFromFavorites ---

    @Test
    @DisplayName("removeGameFromFavorites() should remove game and save user")
    public void removeGameFromFavorites_shouldRemoveGameAndSaveUser() {
        // Arrange
        RegisteredUser user = em.find(RegisteredUser.class, userId);
        assertThat(user.getFavoriteBoardGames()).hasSize(1); // Check pre-condition

        // Act
        sut.removeGameFromFavorites(user, wingspanId); // Remove Wingspan
        em.flush();
        em.clear();

        // Assert
        RegisteredUser updatedUser = em.find(RegisteredUser.class, userId);
        assertThat(updatedUser.getFavoriteBoardGames()).isEmpty(); // Check post-condition
    }

    @Test
    @DisplayName("removeGameFromFavorites() should throw EntityNotFoundException when not a favorite")
    public void removeGameFromFavorites_shouldThrowException_whenNotFavorite() {
        // Arrange
        RegisteredUser user = em.find(RegisteredUser.class, userId);

        // Act & Assert
        // Catan is not a favorite
        assertThrows(EntityNotFoundException.class, () -> {
            sut.removeGameFromFavorites(user, catanId);
        });
    }

    // --- Tests for viewBoardGameDetails ---

    @Test
    @DisplayName("viewBoardGameDetails() should run without exception")
    public void viewBoardGameDetails_shouldRunAndCallMock() {
        // Arrange
        BoardGame game = em.find(BoardGame.class, catanId);

        // Act
        sut.viewBoardGameDetails(game);

        // Assert
        // Test passes if no exception is thrown (we can't test System.out)
    }

    @Test
    @DisplayName("viewBoardGameDetails() should throw EntityNotFoundException for null")
    public void viewBoardGameDetails_shouldThrowException_whenNull() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            sut.viewBoardGameDetails(null);
        });
    }

    // --- Tests for listAllFavoriteBoardGame ---

    @Test
    @DisplayName("listAllFavoriteBoardGame() should return list of names")
    public void listAllFavoriteBoardGame_shouldReturnNameList() {
        // Act
        List<String> favoriteNames = sut.listAllFavoriteBoardGame(userId);

        // Assert
        assertThat(favoriteNames).isNotNull().hasSize(1);
        assertThat(favoriteNames).containsExactly("Wingspan");
    }


}