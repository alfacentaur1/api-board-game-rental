package cz.cvut.fel.ear.serviceTests;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.model.BoardGameState;
import cz.cvut.fel.ear.service.BoardGameItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BoardGameItemServiceTest {

    @Mock
    private BoardGameItemRepository boardGameItemRepository;

    @Mock
    private BoardGameRepository boardGameRepository;

    @InjectMocks
    private BoardGameItemService boardGameItemService;

    private BoardGame boardGame;
    private BoardGameItem forLoanItem;
    private BoardGameItem borrowedItem;

    @BeforeEach
    public void setUp() {
        // Initialize a sample board game
        boardGame = new BoardGame();
        boardGame.setId(1L);
        boardGame.setName("Catan");

        // Create one item available for loan
        forLoanItem = new BoardGameItem();
        forLoanItem.setId(10L);
        forLoanItem.setBoardGame(boardGame);
        forLoanItem.setState(BoardGameState.FOR_LOAN);

        // Create one item that is borrowed
        borrowedItem = new BoardGameItem();
        borrowedItem.setId(11L);
        borrowedItem.setBoardGame(boardGame);
        borrowedItem.setState(BoardGameState.BORROWED);

        // Assign all items to board game
        boardGame.setAvailableStockItems(Arrays.asList(forLoanItem, borrowedItem));
    }

    // ----------------------------
    // Tests for avalaibleItemsInStockNumber
    // ----------------------------

    @Test
    public void avalaibleItemsInStockNumberThrowsEntityNotFoundExceptionWhenNameIsNull() {
        BoardGame noNameGame = new BoardGame();
        when(boardGameRepository.findById(1L)).thenReturn(Optional.of(noNameGame));

        assertThrows(EntityNotFoundException.class, () ->
                boardGameItemService.avalaibleItemsInStockNumber(1L));
    }

    @Test
    public void avalaibleItemsInStockNumberReturnsCorrectCount() {
        when(boardGameRepository.findById(1L)).thenReturn(Optional.of(boardGame));
        when(boardGameRepository.findBoardGameById(1L)).thenReturn(boardGame);

        int count = boardGameItemService.avalaibleItemsInStockNumber(1L);

        assertEquals(1, count); // only FOR_LOAN item is counted
    }

    // ----------------------------
    // Tests for getAllBoardGameItemsForBoardGame
    // ----------------------------

    @Test
    public void getAllBoardGameItemsForBoardGameThrowsEntityNotFoundExceptionForInvalidId() {
        when(boardGameRepository.findBoardGameById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () ->
                boardGameItemService.getAllBoardGameItemsForBoardGame(99L));
    }

    @Test
    public void getAllBoardGameItemsForBoardGameReturnsAllItems() {
        when(boardGameRepository.findBoardGameById(1L)).thenReturn(boardGame);

        List<BoardGameItem> result = boardGameItemService.getAllBoardGameItemsForBoardGame(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(forLoanItem));
        assertTrue(result.contains(borrowedItem));
    }

    // ----------------------------
    // Tests for getAllAvalableBoardGameItemsForBoardGame
    // ----------------------------

    @Test
    public void getAllAvalableBoardGameItemsForBoardGameThrowsEntityNotFoundExceptionForNoName() {
        BoardGame noNameGame = new BoardGame();
        when(boardGameRepository.findById(1L)).thenReturn(Optional.of(noNameGame));

        assertThrows(EntityNotFoundException.class, () ->
                boardGameItemService.getAllAvalableBoardGameItemsForBoardGame(1L));
    }

    @Test
    public void getAllAvalableBoardGameItemsForBoardGameReturnsOnlyForLoanItems() {
        when(boardGameRepository.findById(1L)).thenReturn(Optional.of(boardGame));
        when(boardGameRepository.findBoardGameById(1L)).thenReturn(boardGame);

        List<BoardGameItem> availableItems = boardGameItemService.getAllAvalableBoardGameItemsForBoardGame(1L);

        assertEquals(1, availableItems.size());
        assertEquals(BoardGameState.FOR_LOAN, availableItems.get(0).getState());
    }

    // ----------------------------
    // Tests for addBoardGameItem
    // ----------------------------

    @Test
    public void addBoardGameItemThrowsEntityNotFoundExceptionForInvalidGame() {
        when(boardGameRepository.findBoardGameById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () ->
                boardGameItemService.addBoardGameItem(99L, "SN-01", BoardGameState.FOR_LOAN));
    }

    @Test
    public void addBoardGameItemThrowsParametersExceptionForNullSerialOrState() {
        when(boardGameRepository.findBoardGameById(1L)).thenReturn(boardGame);

        assertThrows(ParametersException.class, () ->
                boardGameItemService.addBoardGameItem(1L, null, BoardGameState.FOR_LOAN));

        assertThrows(ParametersException.class, () ->
                boardGameItemService.addBoardGameItem(1L, "SN-01", null));
    }

    @Test
    public void addBoardGameItemSavesAndReturnsId() {
        when(boardGameRepository.findBoardGameById(1L)).thenReturn(boardGame);

        BoardGameItem saved = new BoardGameItem();
        saved.setId(123L);
        when(boardGameItemRepository.save(any(BoardGameItem.class))).thenReturn(saved);

        long id = boardGameItemService.addBoardGameItem(1L, "SN-123", BoardGameState.FOR_LOAN);

        verify(boardGameItemRepository, times(1)).save(any(BoardGameItem.class));
        assertEquals(123L, id);
    }

    // ----------------------------
    // Tests for updateBoardGameItemState
    // ----------------------------

    @Test
    public void updateBoardGameItemStateThrowsEntityNotFoundExceptionWhenItemNotFound() {
        when(boardGameItemRepository.getBoardGameItemById(42L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () ->
                boardGameItemService.updateBoardGameItemState(42L, BoardGameState.BORROWED));
    }

    @Test
    public void updateBoardGameItemStateThrowsParametersExceptionForInvalidState() {
        // Repository returns existing item
        when(boardGameItemRepository.getBoardGameItemById(forLoanItem.getId())).thenReturn(forLoanItem);

        // Create fake state by null (since enum can't be invalid)
        assertThrows(ParametersException.class, () ->
                boardGameItemService.updateBoardGameItemState(forLoanItem.getId(), null));
    }

    @Test
    public void updateBoardGameItemStateUpdatesSuccessfully() {
        when(boardGameItemRepository.getBoardGameItemById(forLoanItem.getId())).thenReturn(forLoanItem);

        boardGameItemService.updateBoardGameItemState(forLoanItem.getId(), BoardGameState.BORROWED);

        verify(boardGameItemRepository, times(1)).save(any(BoardGameItem.class));
        assertEquals(BoardGameState.BORROWED, forLoanItem.getState());
    }
}
