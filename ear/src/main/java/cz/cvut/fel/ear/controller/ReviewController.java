package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.ReviewDetailDTO;
import cz.cvut.fel.ear.mapper.ReviewMapper;
import cz.cvut.fel.ear.model.Review;
import cz.cvut.fel.ear.service.ReviewService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
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

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDetailDTO> getReviewById(@PathVariable("id") Long id) {
        Review reviewDetailDTO = reviewService.findReviewById(id);
        return ResponseEntity.ok(reviewMapper.toReviewDetailDTO(reviewDetailDTO));
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<List<ReviewDetailDTO>> getReviewsByGameId(@PathVariable("gameId") Long gameId) {
        List<Review> reviews = reviewService.getReviewsForBoardGame(gameId);
        List<ReviewDetailDTO> reviewDTOS = reviews.stream()
                .map(reviewMapper::toReviewDetailDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviewDTOS);

    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/games/{gameId}/users/{userId}")
    public ResponseEntity<?> createReview(@PathVariable("gameId") Long gameId,
                                                        @PathVariable("userId") Long userId,
                                                        @RequestParam("content") String content,
                                                        @RequestParam("rating") int rating){
        Review review = reviewService.createReview(userId, gameId, content, rating);
        HttpHeaders responseHeaders = new HttpHeaders();
        ReviewDetailDTO reviewDetailDTO = reviewMapper.toReviewDetailDTO(review);
        URI location = URI.create(String.format("api/reviews/%d", review.getId()));
        responseHeaders.setLocation(location);
        return ResponseEntity.created(location).headers(responseHeaders).body(reviewDetailDTO);
    }

    // delete review only if the user is the owner of the review
    @PreAuthorize("hasRole('USER') and @reviewSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable("id") Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }


}
