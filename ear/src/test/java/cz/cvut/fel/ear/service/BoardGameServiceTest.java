package cz.cvut.fel.ear.service;
import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.exception.*;

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

import java.util.ArrayList;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model") // Scan for services and DAOs
@ActiveProfiles("test")
public class BoardGameServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BoardGameService sut;

    @Autowired
    private BoardGameRepository boardGameRepo;

    @Autowired
    private UserRepository userRepo;

    // --- Test Data Entities ---
    private RegisteredUser testUser;
    private BoardGame testGame1_Catan;
    private BoardGame testGame2_Wingspan;
    private long catanId;
    private long wingspanId;
    private long userId;

    @BeforeEach
    void setUp() {

        // 1. Create a user
        testUser = new RegisteredUser();
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
    void getBoardGame_shouldReturnGame_whenExists() {
        // Act
        BoardGame found = sut.getBoardGame(catanId);
        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Catan");
    }

    @Test
    @DisplayName("getBoardGame() should throw EntityNotFoundException when game not found")
    void getBoardGame_shouldThrowException_whenNotFound() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            sut.getBoardGame(-99L);
        });
    }

    // --- Tests for getAllBoardGames ---

    @Test
    @DisplayName("getAllBoardGames() should return list of games")
    void getAllBoardGames_shouldReturnList() {
        // Act
        List<BoardGame> games = sut.getAllBoardGames();
        // Assert
        assertThat(games).isNotNull().hasSize(2);
        assertThat(games).extracting(BoardGame::getName).contains("Catan", "Wingspan");
    }

    @Test
    @DisplayName("getAllBoardGames() should throw EntityNotFoundException when list is empty")
    void getAllBoardGames_shouldThrowException_whenEmpty() {
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
    void createBoardGame_shouldCreateGame_whenValid() {
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
    void createBoardGame_shouldThrowException_whenDuplicate() {
        // Act & Assert
        assertThrows(EntityAlreadyExistsException.class, () -> {
            sut.createBoardGame("Catan", "Another description");
        });
    }

    @Test
    @DisplayName("createBoardGame() should throw ParametersException for empty name")
    void createBoardGame_shouldThrowException_whenNameIsEmpty() {
        // Act & Assert
        assertThrows(ParametersException.class, () -> {
            sut.createBoardGame("", "Description");
        });
    }

    // --- Tests for removeBoardGame ---

    @Test
    @DisplayName("removeBoardGame() should delete game when exists")
    void removeBoardGame_shouldCallDelete_whenExists() {
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
    void updateBoardGameDescription_shouldChangeDescription() {
        // Arrange
        String newDescription = "New Description";

        // Act
        sut.updateBoardGameDescription(catanId, newDescription);
        em.flush(); // Commit the transaction
        em.clear(); // Clear cache to force a reload

        // Assert
        BoardGame updated = em.find(BoardGame.class, catanId);
        assertThat(updated.getDescription()).isEqualTo(newDescription);
    }

    // --- Tests for addGameToFavorites ---

    @Test
    @DisplayName("addGameToFavorites() should add game and save user")
    void addGameToFavorites_shouldAddGameAndSaveUser() {
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
    void addGameToFavorites_shouldThrowException_whenDuplicate() {
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
    void removeGameFromFavorites_shouldRemoveGameAndSaveUser() {
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
    void removeGameFromFavorites_shouldThrowException_whenNotFavorite() {
        // Arrange
        RegisteredUser user = em.find(RegisteredUser.class, userId);

        // Act & Assert
        // Catan is not a favorite
        assertThrows(ItemNotInResource.class, () -> {
            sut.removeGameFromFavorites(user, catanId);
        });
    }

    // --- Tests for viewBoardGameDetails ---

    @Test
    @DisplayName("viewBoardGameDetails() should run without exception")
    void viewBoardGameDetails_shouldRunAndCallMock() {
        // Arrange
        BoardGame game = em.find(BoardGame.class, catanId);

        // Act
        sut.viewBoardGameDetails(game);

        // Assert
        // Test passes if no exception is thrown (we can't test System.out)
    }

    @Test
    @DisplayName("viewBoardGameDetails() should throw EntityNotFoundException for null")
    void viewBoardGameDetails_shouldThrowException_whenNull() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            sut.viewBoardGameDetails(null);
        });
    }

    // --- Tests for listAllFavoriteBoardGame ---

    @Test
    @DisplayName("listAllFavoriteBoardGame() should return list of names")
    void listAllFavoriteBoardGame_shouldReturnNameList() {
        // Act
        List<String> favoriteNames = sut.listAllFavoriteBoardGame(userId);

        // Assert
        assertThat(favoriteNames).isNotNull().hasSize(1);
        assertThat(favoriteNames).containsExactly("Wingspan");
    }


}