package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.BoardGameItemCreationDTO;
import cz.cvut.fel.ear.dto.BoardGameItemDTO;
import cz.cvut.fel.ear.dto.BoardGameItemStateUpdateDTO;
import cz.cvut.fel.ear.mapper.BoardGameItemMapper;
import cz.cvut.fel.ear.model.BoardGameItem;
import cz.cvut.fel.ear.service.BoardGameItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BoardGameItemController {
    private final BoardGameItemService boardGameItemService;
    private final BoardGameItemMapper boardGameItemMapper;

    public BoardGameItemController(BoardGameItemService boardGameItemService, BoardGameItemMapper boardGameItemMapper) {
        this.boardGameItemService = boardGameItemService;
        this.boardGameItemMapper = boardGameItemMapper;
    }

    /**
     * Retrieves the number of board game items currently available in stock for a specific board game.
     *
     * @param id The ID of the board game.
     * @return A ResponseEntity containing the count of available items.
     */
    @Operation(summary = "Get Stock Count for Board Game", description = "Retrieves the number of board game items currently available in stock for a specific board game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock count successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/boardgames/{id}/items/stock")
    public ResponseEntity<Map<String, Object>> numberOfStockItemsForGame(
            @Parameter(description = "ID of the board game to get stock count for", example = "1", required = true)
            @PathVariable Long id) {
        int stockCount = boardGameItemService.availableItemsInStockNumber(id);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "BoardGameItems");
        generator.addResponseData("items", stockCount);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all board game items for a specific board game, regardless of their state.
     *
     * @param id The ID of the board game.
     * @return A ResponseEntity containing a list of BoardGameItemDTOs.
     */
    @Operation(summary = "Get All Items for Board Game", description = "Retrieves all board game items for a specific board game, regardless of their state or availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game Items successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/boardgames/{id}/items/all")
    public ResponseEntity<Map<String, Object>> allItemsForGame(
            @Parameter(description = "ID of the board game to retrieve all items for", example = "1", required = true)
            @PathVariable Long id) {
        List<BoardGameItem> boardGameItems = boardGameItemService.getAllBoardGameItemsForBoardGame(id);
        List<BoardGameItemDTO> boardGameItemDTOS = boardGameItems.stream()
                .map(boardGameItemMapper::toDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "BoardGameItem");
        generator.addResponseData("count", boardGameItemDTOS.size());
        generator.addResponseData("items", boardGameItemDTOS);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all available board game items for a specific board game that are currently not on loan.
     *
     * @param id The ID of the board game.
     * @return A ResponseEntity containing a list of available BoardGameItemDTOs.
     */
    @Operation(summary = "Get Available Items for Board Game", description = "Retrieves all available board game items for a specific board game that are currently not on loan and can be borrowed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available Board Game Items successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/boardgames/{id}/items/available")
    public ResponseEntity<Map<String, Object>> availableItemsForGame(
            @Parameter(description = "ID of the board game to retrieve available items for", example = "1", required = true)
            @PathVariable Long id) {
        List<BoardGameItem> boardGameItems = boardGameItemService.getAllAvailableBoardGameItemsForBoardGame(id);
        List<BoardGameItemDTO> boardGameItemDTOS = boardGameItems.stream()
                .map(boardGameItemMapper::toDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "AvailableBoardGameItem");
        generator.addResponseData("count", boardGameItemDTOS.size());
        generator.addResponseData("items", boardGameItemDTOS);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Creates a new board game item in the system.
     *
     * @param boardGameItemCreationDTO Data transfer object containing details for the new item.
     * @return A ResponseEntity indicating success and the location of the new resource.
     */
    @Operation(summary = "Create Board Game Item", description = "Creates a new board game item in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Board Game Item successfully created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Board Game Item with this serial number already exists", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addBoardGameItem(
            @Valid @RequestBody BoardGameItemCreationDTO boardGameItemCreationDTO
    ) {
        long idItem = boardGameItemService.addBoardGameItem(boardGameItemCreationDTO.boardGameId(), boardGameItemCreationDTO.serialNumber(), boardGameItemCreationDTO.state());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "BoardGameItem");

        HttpHeaders responseHeaders = new HttpHeaders();
        URI location = URI.create("/api/items/" + idItem);
        responseHeaders.setLocation(location);

        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }

    /**
     * Updates the state of a specific board game item.
     *
     * @param dto Data transfer object containing the item ID and new state.
     * @return A ResponseEntity indicating success.
     */
    @Operation(summary = "Update Board Game Item State", description = "Updates the state of a specific board game item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game Item state successfully updated", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game Item not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/items/state")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBoardGameItemState(
            @Valid @RequestBody BoardGameItemStateUpdateDTO dto
    ) {
        boardGameItemService.updateBoardGameItemState(dto.itemId(), dto.state());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_MODIFIED, "BoardGameItem");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }
    /**
     * Deletes a specific board game item from the system.
     *
     * @param itemId The ID of the board game item to delete.
     * @return A ResponseEntity indicating the item was deleted.
     */
    @Operation(summary = "Delete Board Game Item", description = "Deletes a specific board game item from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game Item successfully deleted", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game Item not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBoardGameItem(
            @Parameter(description = "ID of the board game item to delete", example = "1", required = true)
            @PathVariable Long itemId) {
        boardGameItemService.deleteBoardGameItem(itemId);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_DELETED, "BoardGameItem");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }
}