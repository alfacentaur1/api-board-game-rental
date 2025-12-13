package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.BoardGameDTO;
import cz.cvut.fel.ear.dto.BoardGameToCreateDTO;
import cz.cvut.fel.ear.dto.BoardGameUpdateDTO;
import cz.cvut.fel.ear.dto.FavoriteCreationDTO;
import cz.cvut.fel.ear.mapper.BoardGameMapper;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.service.BoardGameService;
import cz.cvut.fel.ear.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/boardgames")
public class BoardGameController {
    private final BoardGameService boardGameService;
    private final UserService userService;
    private final BoardGameMapper boardGameMapper;

    public BoardGameController(BoardGameService boardGameService, UserService userService, BoardGameMapper boardGameMapper) {
        this.boardGameService = boardGameService;
        this.userService = userService;
        this.boardGameMapper = boardGameMapper;
    }

    /**
     * Retrieves the details of a specific Board Game by its ID.
     *
     * @param id The ID of the board game to retrieve.
     * @return A ResponseEntity containing the BoardGameDTO.
     */
    @Operation(summary = "Get Board Game by ID", description = "Retrieves the details of a specific Board Game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Conflict occurred", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBoardGame(
            @Parameter(description = "ID of the board game to retrieve", example = "1", required = true)
            @PathVariable Long id
    ) {
        BoardGame boardGame = boardGameService.getBoardGame(id);
        BoardGameDTO DTO = boardGameMapper.toDto(boardGame);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, boardGame.getClass().getSimpleName());
        generator.addResponseData("BoardGame", DTO);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Creates a new board game in the system.
     *
     * @param boardGameToCreateDTO Data transfer object containing the name and description of the new game.
     * @return A ResponseEntity indicating success and the location of the new resource.
     */
    @Operation(summary = "Create Board Game", description = "Creates a new board game in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Board Game successfully created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Board Game already exists", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addBoardGame(
            @Valid @RequestBody BoardGameToCreateDTO boardGameToCreateDTO) {
        Long id = boardGameService.createBoardGame(boardGameToCreateDTO.name(), boardGameToCreateDTO.description());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, BoardGame.class.getSimpleName());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.LOCATION, "/boardGames/" + id);
        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }

    /**
     * Deletes a board game from the system.
     *
     * @param gameId The ID of the board game to delete.
     * @return A ResponseEntity indicating the result of the deletion.
     */
    @Operation(summary = "Delete Board Game", description = "Deletes a board game from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game successfully deleted", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{gameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBoardGame(
            @Parameter(description = "ID of the board game to delete", example = "1", required = true)
            @PathVariable Long gameId) {
        boardGameService.removeBoardGame(gameId);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_DELETED, BoardGame.class.getSimpleName());

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Updates the description of an existing board game.
     *
     * @param boardGameUpdateDTO Data transfer object containing the ID and new description.
     * @return A ResponseEntity indicating success.
     */
    @Operation(summary = "Update Board Game", description = "Updates the description of an existing board game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game successfully updated", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBoardGame(
            @Valid @RequestBody BoardGameUpdateDTO boardGameUpdateDTO) {
        boardGameService.updateBoardGameDescription(boardGameUpdateDTO.id(), boardGameUpdateDTO.description());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_MODIFIED, BoardGame.class.getSimpleName());

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all board games currently in the system.
     *
     * @return A ResponseEntity containing a list of all BoardGameDTOs.
     */
    @Operation(summary = "Get All Board Games", description = "Retrieves all board games currently in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Games successfully retrieved", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAllBoardGames() {
        List<BoardGame> boardGames = boardGameService.getAllBoardGames();
        List<BoardGameDTO> boardGameDTOs = boardGames.stream()
                .map(boardGameMapper::toDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.addResponseData("BoardGame", boardGameDTOs);
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, BoardGame.class.getSimpleName());

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Adds a board game to the authenticated user's list of favorite games.
     *
     * @param favoriteDto The DTO containing username and game ID.
     * @return A ResponseEntity indicating the item was added.
     */
    @Operation(summary = "Add Board Game to Favorites", description = "Adds a board game to the authenticated user's list of favorite games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Board Game successfully added to favorites", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User or Board Game not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Board Game already in user's favorites", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/favorites")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    public ResponseEntity<Map<String, Object>> addFavoriteBoardGame(
            @Valid @RequestBody FavoriteCreationDTO favoriteDto,
            Principal principal) {

        String username = principal.getName();
        RegisteredUser user = (RegisteredUser) userService.getUserByUsername(username);
        boardGameService.addGameToFavorites(user, favoriteDto.gameId());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_ITEM_ADDED_TO_SOURCE, "BoardGame", "Favorites");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.LOCATION, "/api/boardgames/users/" + username + "/favorites/");

        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }
    /**
     * Removes a board game from the authenticated user's list of favorite games.
     *
     * @param gameId   The ID of the board game to remove from favorites.
     * @return A ResponseEntity indicating the item was removed.
     */
    @Operation(summary = "Remove Board Game from Favorites", description = "Removes a board game from the authenticated user's list of favorite games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game successfully removed from favorites", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User or Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/users/favorites/{gameId}")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    public ResponseEntity<Map<String, Object>> deleteGameFromFavorites(
            @Parameter(description = "ID of the board game to remove from favorites", example = "1", required = true)
            @PathVariable Long gameId,
            Principal principal) {
        RegisteredUser user = (RegisteredUser) userService.getUserByUsername(principal.getName());
        boardGameService.removeGameFromFavorites(user, gameId);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_ITEM_REMOVED_FROM_SOURCE, "Board Game", "Favorites");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all board games in the authenticated user's favorites list.
     * @param principal The security principal of the authenticated user.
     * @return A ResponseEntity containing a list of favorite BoardGameDTOs.
     */
    @Operation(summary = "Get Favorite Board Games", description = "Retrieves all board games in the authenticated user's favorites list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite Board Games successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/users/favorites/")
    @PreAuthorize("isAuthenticated() and (hasRole('USER'))")
    public ResponseEntity<Map<String, Object>> getFavorites(
            Principal principal
    ) {
        RegisteredUser user = (RegisteredUser) userService.getUserByUsername(principal.getName());
        List<BoardGameDTO> favoriteGameDTOs = user.getFavoriteBoardGames().stream()
                .map(boardGameMapper::toDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Board Game");
        generator.addResponseData("amount", favoriteGameDTOs.size());
        generator.addResponseData("favorites", favoriteGameDTOs);
        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves the top X most borrowed board games.
     *
     * @param count The number of top borrowed games to retrieve.
     * @return A ResponseEntity containing the list of top borrowed games.
     */
    @Operation(summary = "Get Top Borrowed Board Games", description = "Retrieves the top X most borrowed board games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top Borrowed Board Games successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred (count must be greater than 0)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/topBorrowed/{count}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTopBorrowedBoardGames(
            @Parameter(description = "Number of top borrowed games to retrieve", example = "10", required = true)
            @Min(1) @PathVariable int count
    ) {
        List<BoardGame> topBorrowedGames = boardGameService.getTopXBorrowedGames(count);
        List<BoardGameDTO> topBorrowedGameDTOs = topBorrowedGames.stream()
                .map(boardGameMapper::toDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, BoardGame.class.getSimpleName());
        generator.addResponseData("boardGame", topBorrowedGameDTOs);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }
}