package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.exception.*;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model")
@ActiveProfiles("test")
public class LoanServiceTest {
    @Autowired
    private TestEntityManager em;

    @MockitoSpyBean
    private BoardGameLoanRepository boardGameLoanRepository;

    @MockitoSpyBean
    private BoardGameItemRepository boardGameItemRepository;

    @Autowired
    private LoanService sut;

    private RegisteredUser testUser;
    private BoardGameLoan testLoan;
    private BoardGame testGame;
    private BoardGameItem availableItem;

    @BeforeEach
    void SetUp() {
        // SetUp a user
        testUser = new RegisteredUser();
        testUser.setUsername("JohnDoe");
        testUser.setEmail("test@test.com");
        testUser.setFullName("John Doe");
        testUser.setKarma(100);

        // SetUp game
        testGame = new BoardGame();
        testGame.setName("Game1");
        testGame.setDescription("Description for game1");

        // SetUp items
        availableItem = new BoardGameItem();
        availableItem.setSerialNumber("ITEM-FROM-SETUP");
        availableItem.setBoardGame(testGame);
        availableItem.setState(BoardGameState.FOR_LOAN);
        testGame.getAvailableStockItems().add(availableItem);

        BoardGameItem availableItem2 = new BoardGameItem();
        availableItem2.setSerialNumber("ITEM-FROM-SETUP2");
        availableItem2.setBoardGame(testGame);
        availableItem2.setState(BoardGameState.FOR_LOAN);
        testGame.getAvailableStockItems().add(availableItem2);

        // SetUp loan
        testLoan = new BoardGameLoan();
        testLoan.setUser(testUser);
        testLoan.setDueDate(LocalDate.now().plusDays(10));
        testLoan.setStatus(Status.PENDING);
        testLoan.getItems().add(availableItem);
        availableItem.setState(BoardGameState.BORROWED);



        em.persist(testUser);
        em.persist(testGame);
        em.persist(availableItem);
        em.persist(availableItem2);
        em.persist(testLoan);

        em.flush();
    }


    @Test
    @DisplayName("Should retrieve a board game loan by id and throw when not found")
    void testGetBoardGameLoan() {
        BoardGameLoan loanFound = sut.getBoardGameLoan(testLoan.getId());

        // Check if loan was found
        assertNotNull(loanFound);
        // Check if found loan is the same as created
        assertEquals(loanFound.getId(), testLoan.getId());

        // Check if correct exception is thrown when loan is not found
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.getBoardGameLoan(-1)
        );
    }

    @Test
    @DisplayName("Should retrieve all board game loans")
    void testGetBoardGameLoans() {
        List<BoardGameLoan> allLoans = sut.getBoardGameLoans();
        // Check if there is testLoan existing
        assertFalse(allLoans.isEmpty());
        assertTrue(allLoans.stream().anyMatch(
                loan -> loan.getId() == testLoan.getId()
        ));
    }

    @Test
    @DisplayName("Should retrieve all board game loans for a specific user")
    void testGetAllBoardGameLoansByUser() {
        List<BoardGameLoan> foundLoans = sut.getAllBoardGameLoansByUser(testUser.getId());

        // Check if loan is found and is the same as when defined
        assertEquals(1, foundLoans.size());
        assertEquals(testLoan.getId(), foundLoans.getFirst().getId());
    }

    @Test
    @DisplayName("Should approve a PENDING loan and throw when loan id is invalid")
    void testApproveBoardGameLoan() {
        sut.approveGameLoan(testLoan.getId());
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.APPROVED, foundLoan.getStatus());

        // Check if correct exception is thrown when incorrect loan id is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.approveGameLoan(-1)
        );
    }

    @Test
    @DisplayName("Should reject a PENDING loan and throw when loan id is invalid")
    void testRejectGameLoan() {
        sut.rejectGameLoan(testLoan.getId());
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.REJECTED, foundLoan.getStatus());

        // Check if correct exception is thrown when incorrect loan id is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.rejectGameLoan(-1)
        );
    }

    @Test
    @DisplayName("Should change a loan's status and throw exception for incorrect input")
    void testChangeLoanStatus() {
        // Status 1
        sut.changeLoanStatus(testLoan.getId(), Status.RETURNED_LATE);
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.RETURNED_LATE, foundLoan.getStatus());

        // Status 2
        sut.changeLoanStatus(testLoan.getId(), Status.RETURNED_IN_TIME);
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan2 = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.RETURNED_IN_TIME, foundLoan2.getStatus());

        // Check if correct exception is thrown when incorrect id or state is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.changeLoanStatus(-1, Status.APPROVED)
        );

        assertThrows(
                InvalidStatusException.class,
                () -> sut.changeLoanStatus(testLoan.getId(), null)
        );
    }

    @Test
    @DisplayName("Should create a loan with valid data and validate parameters")
    void testCreateBoardGameLoan() {
        LocalDate dueDate = LocalDate.now().plusDays(7);
        List<String> gameNames = List.of("Game1");

        // Create new loan
        long newLoanId = sut.createLoan(dueDate, gameNames, testUser.getId());

        // Find the loan and check it properties
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, newLoanId);
        assertNotNull(foundLoan);
        assertEquals(Status.PENDING, foundLoan.getStatus());
        assertEquals(1, foundLoan.getItems().size());
        assertEquals(testUser.getId(), foundLoan.getUser().getId());

        // Check user has loan binded to him
        assertTrue(testUser.getBoardGameLoans().contains(foundLoan));

        // Check if correct exception is thrown when incorrect dueDate, empty Names list are given
        assertThrows(
                InvalidDateException.class,
                () -> sut.createLoan(LocalDate.now().minusDays(1), gameNames, testUser.getId())
        );
        assertThrows(
                ParametersException.class,
                () -> sut.createLoan(LocalDate.now().plusDays(2), Collections.emptyList(), testUser.getId())
        );
    }

    @Test
    @DisplayName("Should return a loan and set items back to FOR_LOAN; validate loan id")
    void testReturnBoardGameLoan() {
        // Approve the loan
        sut.approveGameLoan(testLoan.getId());
        // Return loan
        sut.returnBoardGameLoan(testLoan.getId());
        em.flush();
        em.refresh(testLoan);

        // Check if board game items state were returned to FOR LOAN
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(BoardGameState.FOR_LOAN, foundLoan.getItems().getFirst().getState());

        // Check if correct exception is thrown when invalid loan id is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.returnBoardGameLoan(-1)
        );
    }

    @Test
    @DisplayName("Should list currently borrowed board game items")
    void testCurrentlyBorrowedBoardGameItems() {
        // Find all borrowed items and check their state
        List<BoardGameItem> borrowedItems = sut.currentlyBorrowedBoardGameItems();
        assertFalse(borrowedItems.isEmpty());
        assertEquals(BoardGameState.BORROWED, borrowedItems.getFirst().getState());
    }

    // BUSINESS TESTS

    @Test
    @DisplayName("Should prevent creating a loan for an already borrowed item")
    void testRentedGameItemCantBeRented() {
        // Create a loan for the available item
        LocalDate dueDate = LocalDate.now().plusDays(5);
        List<String> gameNames = List.of(testGame.getName());

        sut.createLoan(dueDate, gameNames, testUser.getId());
        em.flush();

        // Confirm the item is now borrowed
        BoardGameItem borrowedItem = em.find(BoardGameItem.class, availableItem.getId());
        assertEquals(BoardGameState.BORROWED, borrowedItem.getState());

        // Try creating second loan for the same item
        assertThrows(
                NotAvalaibleInStockException.class,
                () -> sut.createLoan(dueDate.plusDays(2), gameNames, testUser.getId())
        );
    }

    @Test
    @DisplayName("Should allow borrowing the same game twice if multiple items exist")
    void testCanBorrowTheSameGameTwice() {
        // Add new item for a game
        availableItem = new BoardGameItem();
        availableItem.setSerialNumber("ITEM-FROM-SETUP");
        availableItem.setBoardGame(testGame);
        availableItem.setState(BoardGameState.FOR_LOAN);
        testGame.getAvailableStockItems().add(availableItem);

        em.persist(availableItem);
        em.persist(testGame);
        em.flush();

        // Create new loan
        LocalDate dueDate = LocalDate.now().plusDays(7);
        List<String> gameNames = List.of("Game1", "Game1");
        long newLoanId = sut.createLoan(dueDate, gameNames, testUser.getId());

        // Find the loan and check it properties
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, newLoanId);
        assertNotNull(foundLoan);
        assertEquals(Status.PENDING, foundLoan.getStatus());
        assertEquals(2, foundLoan.getItems().size());
        assertEquals(testUser.getId(), foundLoan.getUser().getId());

        // Check user has loan bound to him
        assertTrue(testUser.getBoardGameLoans().contains(foundLoan));
    }

    @Test
    @DisplayName("Test positive loan lifecycle")
    void testLoanLifeCycle_APPROVED() {
        // Check that items are in state borrowed
        List<BoardGameItem> borrowedItems = testLoan.getItems();

        for (BoardGameItem gameItem : borrowedItems) {
            assertEquals(BoardGameState.BORROWED, gameItem.getState());
        }
        // Check that loan has correct state
        assertEquals(Status.PENDING, testLoan.getStatus());

        // Approve loan
        sut.approveGameLoan(testLoan.getId());


        BoardGameLoan foundLoan = sut.getBoardGameLoan(testLoan.getId());

        // Check that loan has correct state
        assertEquals(Status.APPROVED, foundLoan.getStatus());

        // Check that items in loan are still borrowed
        List<BoardGameItem> foundItems = foundLoan.getItems();
        for (BoardGameItem foundItem : foundItems) {
            assertEquals(BoardGameState.BORROWED, foundItem.getState());
        }
    }

    @Test
    @DisplayName("Test negative loan lifecycle")
    void testLoanLifeCycle_rejected() {
        // Check that items are in state borrowed
        List<BoardGameItem> borrowedItems = testLoan.getItems();

        for (BoardGameItem gameItem : borrowedItems) {
            assertEquals(BoardGameState.BORROWED, gameItem.getState());
        }

        // Check that order has correct state
        assertEquals(Status.PENDING, testLoan.getStatus());

        // Reject loan
        sut.rejectGameLoan(testLoan.getId());

        BoardGameLoan foundLoan = sut.getBoardGameLoan(testLoan.getId());
        // Check that loan has correct state
        assertEquals(Status.REJECTED, foundLoan.getStatus());

        // Check that items have correct state
        List<BoardGameItem> foundItems = foundLoan.getItems();
        for (BoardGameItem foundItem : foundItems) {
            assertEquals(BoardGameState.FOR_LOAN, foundItem.getState());
        }
    }
}
