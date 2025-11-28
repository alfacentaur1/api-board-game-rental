package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.BoardGameDTO;
import cz.cvut.fel.ear.dto.BoardGameItemCreationDTO;
import cz.cvut.fel.ear.dto.BoardGameItemDTO;
import cz.cvut.fel.ear.dto.BoardGameItemStateDTO;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.service.BoardGameItemService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BoardGameItemController {
    private final BoardGameItemService boardGameItemService;

    public BoardGameItemController(BoardGameItemService boardGameItemService) {
        this.boardGameItemService = boardGameItemService;
    }

    @GetMapping("/boardgames/{id}/items/stock")
    public ResponseEntity<Integer> numberOfStockItemsForGame(@PathVariable int id) {
        int stockCount = boardGameItemService.availableItemsInStockNumber(id);
        return new ResponseEntity<>(stockCount, HttpStatus.OK);
    }

    @GetMapping("/boardgames/{id}/items/all")
    public ResponseEntity<List<BoardGameItemDTO>> allItemsForGame(@PathVariable Long id) {
        List<BoardGameItem> boardGameItems = boardGameItemService.getAllBoardGameItemsForBoardGame(id);
        List<BoardGameItemDTO> boardGameItemDTOS = boardGameItems.stream()
                .map(boardGameItem -> new BoardGameItemDTO(
                        boardGameItem.getId(),
                        boardGameItem.getSerialNumber(),
                        boardGameItem.getState(),
                        boardGameItem.getBoardGame().getName()

                ))
                .toList();
        return new ResponseEntity<>(boardGameItemDTOS, HttpStatus.OK);
    }

    @GetMapping("/boardgames/{id}/items/available")
    public ResponseEntity<List<BoardGameItemDTO>> availableItemsForGame(@PathVariable Long id) {
        List<BoardGameItem> boardGameItems = boardGameItemService.getAllAvailableBoardGameItemsForBoardGame(id);
        List<BoardGameItemDTO> boardGameItemDTOS = boardGameItems.stream()
                .map(boardGameItem -> new BoardGameItemDTO(
                        boardGameItem.getId(),
                        boardGameItem.getSerialNumber(),
                        boardGameItem.getState(),
                        boardGameItem.getBoardGame().getName()
                ))
                .toList();
        return new ResponseEntity<>(boardGameItemDTOS, HttpStatus.OK);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBoardGameItem( @RequestBody BoardGameItemCreationDTO boardGameItemCreationDTO) {
        Long idItem = boardGameItemService.addBoardGameItem(boardGameItemCreationDTO.boardGameId(),boardGameItemCreationDTO.serialNumber(),boardGameItemCreationDTO.state());
        URI location = URI.create("/api/items/" + idItem);

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateBoardGameItemState(@PathVariable Long itemId,
            @RequestBody BoardGameItemStateDTO boardGameItemStateDTO) {

        boardGameItemService.updateBoardGameItemState(itemId,
                boardGameItemStateDTO.boardGameState());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoardGameItem( @PathVariable Long itemId) {
        boardGameItemService.deleteBoardGameItem(itemId);
        return ResponseEntity.noContent().build();
    }

}
