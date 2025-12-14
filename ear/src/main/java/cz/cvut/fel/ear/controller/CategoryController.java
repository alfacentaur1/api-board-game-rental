package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.CategoryCreationDTO;
import cz.cvut.fel.ear.dto.CategoryDTO;
import cz.cvut.fel.ear.dto.CategoryGameDTO;
import cz.cvut.fel.ear.model.Category;
import cz.cvut.fel.ear.service.CategoryService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Creates a new category in the system.
     *
     * @param categoryCreationDTO Data transfer object containing the name of the category.
     * @return A ResponseEntity indicating success and the location of the new category.
     */
    @Operation(summary = "Create Category", description = "Creates a new category in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category successfully created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Category already exists", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody CategoryCreationDTO categoryCreationDTO
    ) {
        long id = categoryService.addCategory(categoryCreationDTO.name());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "Category");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", "api/categories/" + id);

        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }

    /**
     * Adds a board game to a specific category.
     *
     * @param dto Data transfer object containing category ID and board game ID.
     * @return A ResponseEntity indicating the game was added to the category.
     */
    @Operation(summary = "Add Board Game to Category", description = "Adds a board game to a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game successfully added to Category", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Category or Board Game not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Board Game already in Category", content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/games")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addBoardGameToCategory(
            @Valid @RequestBody CategoryGameDTO dto
    ) {
        categoryService.addBoardGameToCategory(dto.boardGameId(), dto.categoryId());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_ITEM_ADDED_TO_SOURCE, "Board Game", "Category");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Removes a board game from a specific category.
     *
     * @param categoryId  The ID of the category.
     * @param boardGameId The ID of the board game to remove.
     * @return A ResponseEntity indicating the game was removed from the category.
     */
    @Operation(summary = "Remove Board Game from Category", description = "Removes a board game from a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board Game successfully removed from Category", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Category or Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{categoryId}/games/{boardGameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeBoardGameFromCategory(
            @Parameter(description = "ID of the category", example = "1", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "ID of the board game to remove from the category", example = "1", required = true)
            @PathVariable Long boardGameId) {
        categoryService.removeGameFromCategory(boardGameId, categoryId);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_ITEM_REMOVED_FROM_SOURCE, "Board Game", "Category");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all categories currently in the system.
     *
     * @return A ResponseEntity containing a list of CategoryDTOs.
     */
    @Operation(summary = "Get All Categories", description = "Retrieves all categories currently in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories successfully retrieved", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Category");
        generator.addResponseData("count", categoryDTOs.size());
        generator.addResponseData("categories", categoryDTOs);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }
}