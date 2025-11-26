package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model")
@ActiveProfiles("test")
public class BoardGameItemServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BoardGameItemService sut;

    private BoardGame boardGame;
    private BoardGameItem testItem; // item created in setUp

    @BeforeEach
    void setUp() {
        // setup a user
        RegisteredUser testUser = new RegisteredUser();
        testUser.setKarma(100);
        testUser.setEmail("test@test.com");
        testUser.setUsername("test");
        testUser.setFullName("Test User");
        em.persist(testUser);

        // setup a category
        Category category = new Category();
        category.setName("Kitten games");
        em.persist(category);

        // setup the main board game
        boardGame = new BoardGame();
        boardGame.setName("Exploding kittens");
        boardGame.setDescription("Exploding game from kickstarter");
        boardGame.getCategories().add(category);

        // setup a review and link it
        Review review = new Review();
        review.setAuthor(testUser);
        review.setComment("Test review text");
        review.setScore(5);
        review.setBoardGame(boardGame);
        boardGame.getRatings().add(review);
        em.persist(boardGame); // persist game (and review via cascade)

        // setup one item (borrowed) and link it
        this.testItem = new BoardGameItem(); // assign to class variable
        this.testItem.setBoardGame(boardGame);
        this.testItem.setSerialNumber("ITEM-FROM-SETUP");
        this.testItem.setState(BoardGameState.BORROWED);

        boardGame.getAvailableStockItems().add(this.testItem); // link parent to child

        em.persist(this.testItem);
        em.merge(boardGame); // save the change to boardGame's item list
        em.flush();
    }

    @Test
    @DisplayName("Should add a new board game item")
    void testAddBoardGameItem() {
        Long boardGameId = boardGame.getId();
        String serialNumber = "SN-TEST-12345";

        // check state before (should have 1 item from setUp)
        assertEquals(1, boardGame.getAvailableStockItems().size());

        sut.addBoardGameItem(boardGameId, serialNumber, BoardGameState.FOR_LOAN);
        em.flush();
        em.refresh(boardGame);

        // check state after (should have 2 items now)
        assertEquals(2, boardGame.getAvailableStockItems().size());

        // get the newly added item (it's the last one)
        BoardGameItem boardGameItem = boardGame.getAvailableStockItems().getLast();

        assertNotNull(boardGameItem);
        assertEquals(serialNumber, boardGameItem.getSerialNumber());
        assertEquals(boardGameId, boardGameItem.getBoardGame().getId());
    }

    @Test
    @DisplayName("Should delete an item and throw exception for non-existing item")
    void testDeleteBoardGameItem(){
        // arrange: get the ID from setUp
        Long itemId = this.testItem.getId();

        // check it exists before deleting
        assertNotNull(em.find(BoardGameItem.class, itemId));
        assertEquals(1, boardGame.getAvailableStockItems().size());

        // act
        sut.deleteBoardGameItem(itemId);
        em.flush();

        // assert: check it's gone from DB
        assertNull(em.find(BoardGameItem.class, itemId));

        // assert: check it's gone from the parent's list
        em.refresh(boardGame);
        assertTrue(boardGame.getAvailableStockItems().isEmpty());

        // exception test: delete non-existing
        assertThrows(EntityNotFoundException.class, () -> {
            sut.deleteBoardGameItem(-1L);
        });
    }

    /**
     * new test for the 3 query methods
     */
    @Test
    @DisplayName("Should correctly query for all, available, and count of items")
    void testGetItemQueries() {
        // arrange: add one more item that IS available
        BoardGameItem availableItem = new BoardGameItem();
        availableItem.setBoardGame(boardGame);
        availableItem.setSerialNumber("ITEM-FOR-LOAN");
        availableItem.setState(BoardGameState.FOR_LOAN);

        boardGame.getAvailableStockItems().add(availableItem);
        em.persist(availableItem);
        em.merge(boardGame);
        em.flush();

        Long boardGameId = boardGame.getId();

        // --- Test 1: getAllBoardGameItemsForBoardGame ---
        // should return 2 items (one BORROWED, one FOR_LOAN)
        List<BoardGameItem> allItems = sut.getAllBoardGameItemsForBoardGame(boardGameId);
        assertEquals(2, allItems.size());

        // --- Test 2: getAllAvailableBoardGameItemsForBoardGame ---
        // should return 1 item (only the FOR_LOAN one)
        List<BoardGameItem> availableItems = sut.getAllAvailableBoardGameItemsForBoardGame(boardGameId);
        assertEquals(1, availableItems.size());
        assertEquals("ITEM-FOR-LOAN", availableItems.getFirst().getSerialNumber());

        // --- Test 3: availableItemsInStockNumber ---
        // should return 1
        int availableCount = sut.availableItemsInStockNumber(boardGameId);
        assertEquals(1, availableCount);
    }
}