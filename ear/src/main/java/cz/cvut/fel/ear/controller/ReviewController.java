package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.ReviewDetailDTO;
import cz.cvut.fel.ear.dto.ReviewToCreateDTO;
import cz.cvut.fel.ear.mapper.ReviewMapper;
import cz.cvut.fel.ear.model.Review;
import cz.cvut.fel.ear.model.User;
import cz.cvut.fel.ear.service.ReviewService;
import cz.cvut.fel.ear.service.UserService;
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
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, ReviewMapper reviewMapper, UserService userService) {
        this.reviewMapper = reviewMapper;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    /**
     * Retrieves detailed information about a specific review by its ID.
     *
     * @param id The ID of the review to retrieve.
     * @return A ResponseEntity containing the ReviewDetailDTO.
     */
    @Operation(summary = "Get Review by ID", description = "Retrieves detailed information about a specific review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewById(
            @Parameter(description = "ID of the review to retrieve", example = "1", required = true)
            @PathVariable("id") Long id) {
        Review reviewDetailDTO = reviewService.findReviewById(id);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Review");
        generator.addResponseData("review", reviewMapper.toReviewDetailDTO(reviewDetailDTO));

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all reviews for a specific board game.
     *
     * @param gameId The ID of the board game.
     * @return A ResponseEntity containing a list of ReviewDetailDTOs.
     */
    @Operation(summary = "Get Reviews by Board Game ID", description = "Retrieves all reviews for a specific board game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Board Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/games/{gameId}")
    public ResponseEntity<Map<String, Object>> getReviewsByGameId(
            @Parameter(description = "ID of the board game to get reviews for", example = "1", required = true)
            @PathVariable("gameId") Long gameId) {
        List<Review> reviews = reviewService.getReviewsForBoardGame(gameId);
        List<ReviewDetailDTO> reviewDTOS = reviews.stream()
                .map(reviewMapper::toReviewDetailDTO)
                .collect(Collectors.toList());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Review");
        generator.addResponseData("reviews", reviewDTOS);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Creates a new review for a board game.
     *
     * @param reviewDto Data transfer object containing review details (userId, gameId, content, score).
     * @return A ResponseEntity indicating success and the location of the new review.
     */
    @Operation(summary = "Create Review", description = "Creates a new review for a board game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review successfully created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User or Board Game not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping("/")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createReview(
            @Valid @RequestBody ReviewToCreateDTO reviewDto,
            Principal principal
    ) {
        User user = userService.getUserByUsername(principal.getName());
        Review review = reviewService.createReview(user.getId(), reviewDto.gameId(), reviewDto.content(), reviewDto.score());
        ReviewDetailDTO reviewDetailDTO = reviewMapper.toReviewDetailDTO(review);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "Review");
        generator.addResponseData("review", reviewDetailDTO);

        HttpHeaders responseHeaders = new HttpHeaders();
        URI location = URI.create(String.format("api/reviews/%d", review.getId()));
        responseHeaders.setLocation(location);

        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }

    /**
     * Deletes a review from the system.
     *
     * @param id The ID of the review to delete.
     * @return A ResponseEntity indicating the review was deleted.
     */
    @Operation(summary = "Delete Review", description = "Deletes a review from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review successfully deleted", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
    })
    @PreAuthorize("isAuthenticated() and (hasRole('USER') and @reviewSecurity.isOwner(#id, authentication)) or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @Parameter(description = "ID of the review to delete", example = "1", required = true)
            @PathVariable("id") Long id) {
        reviewService.deleteReview(id);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_DELETED, "Review");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }
}