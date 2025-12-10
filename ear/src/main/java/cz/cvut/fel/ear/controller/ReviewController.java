package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.ReviewDetailDTO;
import cz.cvut.fel.ear.mapper.ReviewMapper;
import cz.cvut.fel.ear.model.Review;
import cz.cvut.fel.ear.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    public ReviewController(ReviewService reviewService, ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
        this.reviewService = reviewService;
    }

    @Operation(
            summary = "Get Review by ID",
            description = "Retrieves detailed information about a specific review"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review successfully retrieved",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error occurred",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewById(
            @Parameter(
                    description = "ID of the review to retrieve",
                    example = "1",
                    required = true
            )
            @PathVariable("id") Long id) {
        Review reviewDetailDTO = reviewService.findReviewById(id);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Review");
        generator.addResponseData("review", reviewMapper.toReviewDetailDTO(reviewDetailDTO));

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    @Operation(
            summary = "Get Reviews by Board Game ID",
            description = "Retrieves all reviews for a specific board game"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews successfully retrieved",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error occurred",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Board Game not found",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @GetMapping("/games/{gameId}")
    public ResponseEntity<Map<String, Object>> getReviewsByGameId(
            @Parameter(
                    description = "ID of the board game to get reviews for",
                    example = "1",
                    required = true
            )
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

    @Operation(
            summary = "Create Review",
            description = "Creates a new review for a board game"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review successfully created",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error occurred",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or Board Game not found",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Authentication required",
                    content = @Content(schema = @Schema(hidden = true))
            ),
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/games/{gameId}/users/{userId}")
    public ResponseEntity<Map<String, Object>> createReview(
            @Parameter(
                    description = "ID of the board game to review",
                    example = "1",
                    required = true
            )
            @PathVariable("gameId") Long gameId,
            @Parameter(
                    description = "ID of the user creating the review",
                    example = "1",
                    required = true
            )
            @PathVariable("userId") Long userId,
            @Parameter(
                    description = "Content of the review",
                    example = "Great game!",
                    required = true
            )
            @RequestParam("content") String content,
            @Parameter(
                    description = "Rating score from 1 to 5",
                    example = "5",
                    required = true
            )
            @RequestParam("rating") int rating
    ) {
        Review review = reviewService.createReview(userId, gameId, content, rating);
        ReviewDetailDTO reviewDetailDTO = reviewMapper.toReviewDetailDTO(review);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "Review");
        generator.addResponseData("review", reviewDetailDTO);

        HttpHeaders responseHeaders = new HttpHeaders();
        URI location = URI.create(String.format("api/reviews/%d", review.getId()));
        responseHeaders.setLocation(location);

        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Delete Review",
            description = "Deletes a review from the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review successfully deleted",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error occurred",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Authentication required",
                    content = @Content(schema = @Schema(hidden = true))
            ),
    })
    @PreAuthorize("hasRole('USER') and @reviewSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @Parameter(
                    description = "ID of the review to delete",
                    example = "1",
                    required = true
            )
            @PathVariable("id") Long id) {
        reviewService.deleteReview(id);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_DELETED, "Review");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }


}
