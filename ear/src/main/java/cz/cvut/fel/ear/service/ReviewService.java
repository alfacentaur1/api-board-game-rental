package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.ReviewRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidCommentRangeException;
import cz.cvut.fel.ear.exception.InvalidRatingScoreException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.model.Review;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import cz.cvut.fel.ear.model.User;
import cz.cvut.fel.ear.service.interfaces.ReviewServiceI;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ReviewService implements ReviewServiceI {
    private final int MAX_RATING = 5;
    private final int MIN_RATING = 0;
    private final int MAX_CONTENT_LENGTH = 200; // 200 characters

    private final BoardGameService gameService;
    private final ReviewRepository reviewRepository;

    public ReviewService(BoardGameService gameService, ReviewRepository reviewRepository) {
        this.gameService = gameService;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<Review> getBoardGameReviews(long gameId) {
        // Check if board game exists
        gameService.getBoardGame(gameId);

        return reviewRepository.findAllByBoardGame_IdIn(Collections.singletonList(gameId));
    }

    @Override
    public Review createReview(User user, long gameId, String content, Integer rating) {
        // Check if board game exist
        BoardGame game = gameService.getBoardGame(gameId);

        // Validate rating input
        validateRatingInput(content, rating);

        // Create new review
        Review newReview = new Review();
        newReview.setBoardGame(game);
        newReview.setComment(content);
        newReview.setScore(rating);
        newReview.setAuthor(user);
        newReview.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(newReview);

        return newReview;
    }

    @Override
    public void updateReview(long id, String content, Integer rating) {
        // Find the review
        Review review = findReview(id).get();

        boolean change = false;

        // Check what to change and change it
        if (content != null) {
            validateContent(content);
            change = true;
            review.setComment(content);
        }
        if (rating != null) {
            validateRating(rating);
            change = true;
            review.setValue(rating);
        }

        // If anything changed update the dateCreated
        if (change) {
            review.setCreatedAt(LocalDateTime.now());
        }

        reviewRepository.save(review);
    }

    @Override
    public void deleteReview(long id) {
        Review reviewToDelete = findReview(id).get();

        reviewRepository.delete(reviewToDelete);
    }

    private Optional<Review> findReview (long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);

        // Check if review was found
        if (review == null) {
            throw new EntityNotFoundException(
                    String.format("Review with id %d not found",reviewId)
            );
        }

        return Optional.of(review);

    }


    private void validateRatingInput(String content, int rating) {
        // Check for null
        if (content == null) {
            throw new ParametersException("content is null");
        }
        validateContent(content);
        validateRating(rating);
    }

    private void validateRating(int rating) {
        // Check rating
        if (rating > MAX_RATING || rating < MIN_RATING) {
            throw new InvalidRatingScoreException(
                    String.format("Rating must be between %d and %d", MAX_RATING,MIN_RATING)
            );
        }
    }

    private void validateContent(String content) {
        // Check content
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidCommentRangeException(
                    String.format("Review content is limited to %d characters", MAX_CONTENT_LENGTH)
            );
        }
    }


}
