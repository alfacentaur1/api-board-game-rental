package cz.cvut.fel.ear.serviceTests;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidDateException;
import cz.cvut.fel.ear.exception.InvalidStatusException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.*;
import cz.cvut.fel.ear.service.LoanService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

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


    @BeforeEach
    void SetUp() {
        // SetUp a user
        testUser = new RegisteredUser();
        testUser.setUsername("JohnDoe");
        testUser.setEmail("test@test.com");
        testUser.setFullName("John Doe");
        testUser.setKarma(100);

        // SetUp game
        BoardGame testGame = new BoardGame();
        testGame.setName("Game1");
        testGame.setDescription("Description for game1");

        // SetUp items
        BoardGameItem availableItem = new BoardGameItem();
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
        testLoan.setDueDate(LocalDateTime.now().plusDays(10));
        testLoan.setStatus(Status.pending);
        testLoan.getItems().add(availableItem2);
        availableItem2.setState(BoardGameState.BORROWED);



        em.persist(testUser);
        em.persist(testGame);
        em.persist(availableItem);
        em.persist(availableItem2);
        em.persist(testLoan);

        em.flush();
    }


    @Test
    public void testGetBoardGameLoan() {
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
    public void testGetBoardGameLoans() {
        List<BoardGameLoan> allLoans = sut.getBoardGameLoans();
        // Check if there is testLoan existing
        assertFalse(allLoans.isEmpty());
        assertTrue(allLoans.stream().anyMatch(
                loan -> loan.getId() == testLoan.getId()
        ));
    }

    @Test
    public void testGetAllBoardGameLoansByUser() {
        List<BoardGameLoan> foundLoans = sut.getAllBoardGameLoansByUser(testUser.getId());

        // Check if loan is found and is the same as when defined
        assertEquals(1, foundLoans.size());
        assertEquals(testLoan.getId(), foundLoans.getFirst().getId());
    }

    @Test
    public void testApproveBoardGameLoan() {
        sut.approveGameLoan(testLoan.getId());
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.approved, foundLoan.getStatus());

        // Check if correct exception is thrown when incorrect loan id is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.approveGameLoan(-1)
        );
    }

    @Test
    public void testRejectGameLoan() {
        sut.rejectGameLoan(testLoan.getId());
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.rejected, foundLoan.getStatus());

        // Check if correct exception is thrown when incorrect loan id is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.rejectGameLoan(-1)
        );
    }

    @Test
    public void testChangeLoanStatus() {
        // Status 1
        sut.changeLoanStatus(testLoan.getId(), Status.returnedLate);
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.returnedLate, foundLoan.getStatus());

        // Status 2
        sut.changeLoanStatus(testLoan.getId(), Status.returnedInTime);
        em.flush();

        // Find the loan and check its state
        BoardGameLoan foundLoan2 = em.find(BoardGameLoan.class, testLoan.getId());
        assertEquals(Status.returnedInTime, foundLoan2.getStatus());

        // Check if correct exception is thrown when incorrect id or state is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.changeLoanStatus(-1, Status.approved)
        );

        assertThrows(
                InvalidStatusException.class,
                () -> sut.changeLoanStatus(testLoan.getId(), null)
        );
    }

    @Test
    public void testCreateBoardGameLoan() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        List<String> gameNames = List.of("Game1");

        // Create new loan
        long newLoanId = sut.createLoan(dueDate, gameNames, testUser.getId());

        // Find the loan and check it properties
        BoardGameLoan foundLoan = em.find(BoardGameLoan.class, newLoanId);
        assertNotNull(foundLoan);
        assertEquals(Status.pending, foundLoan.getStatus());
        assertEquals(1, foundLoan.getItems().size());
        assertEquals(testUser.getId(), foundLoan.getUser().getId());

        // Check if correct exception is thrown when incorrect dueDate, empty Names list are given
        assertThrows(
                InvalidDateException.class,
                () -> sut.createLoan(LocalDateTime.now().minusDays(1), gameNames, testUser.getId())
        );
        assertThrows(
                ParametersException.class,
                () -> sut.createLoan(LocalDateTime.now().plusDays(2), Collections.emptyList(), testUser.getId())
        );
    }

    @Test
    public void testReturnBoardGameLoan() {
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
    public void testCurrentlyBorrowedBoardGameItems() {
        // Find all borrowed items and check their state
        List<BoardGameItem> borrowedItems = sut.currentlyBorrowedBoardGameItems();
        assertFalse(borrowedItems.isEmpty());
        assertEquals(BoardGameState.BORROWED, borrowedItems.getFirst().getState());
    }
}
