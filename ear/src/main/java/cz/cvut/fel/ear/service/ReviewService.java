package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.ReviewRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidCommentRangeException;
import cz.cvut.fel.ear.exception.InvalidRatingScoreException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.Admin;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.model.Review;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
    private final UserService userService;
    private final ReviewRepository reviewRepository;
    private final BoardGameService boardGameService;
    private final int maxRating = 5;
    private final int minRating = 0;
    private final int maxCommentRange = 200;

    public ReviewService(ReviewRepository reviewRepository, BoardGameService boardGameService, @Lazy UserService userService) {
        this.reviewRepository = reviewRepository;
        this.boardGameService = boardGameService;
        this.userService = userService;
    }

    /**
     * Retrieves a review by its ID.
     *
     * @param reviewId the ID of the review
     * @return the Review entity
     * @throws EntityNotFoundException if the review is not found
     */
    public Review findReviewById(long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(Review.class.getSimpleName(), reviewId));
    }

    /**
     * Retrieves all reviews associated with a specific board game.
     *
     * @param gameId the ID of the board game
     * @return a list of Review entities
     */
    public List<Review> getReviewsForBoardGame(long gameId) {
        BoardGame boardGame = boardGameService.getBoardGame(gameId);

        return reviewRepository.findAllByBoardGame_IdIn(List.of(gameId));
    }

    /**
     * Creates a new review for a board game by a registered user.
     *
     * @param userId      the ID of the author
     * @param gameId      the ID of the board game being reviewed
     * @param content     the text content of the review
     * @param ratingValue the numerical score (0-5)
     * @return the created Review entity
     * @throws IllegalArgumentException if the user is an Admin
     */
    @Transactional
    public Review createReview(long userId, long gameId, String content, int ratingValue) {
        if (userService.findById(userId) instanceof Admin) {
            throw new IllegalArgumentException("Could not be cast on admin");
        }
        RegisteredUser user = (RegisteredUser) userService.findById(userId);

        BoardGame boardGame = boardGameService.getBoardGame(gameId);

        validateReviewInput(content, ratingValue);

        Review newReview = new Review();
        newReview.setBoardGame(boardGame);
        newReview.setComment(content);
        newReview.setScore(ratingValue);
        newReview.setCreatedAt(LocalDateTime.now());
        newReview.setAuthor(user);

        reviewRepository.save(newReview);

        userService.linkReviewToUser(userId, newReview.getId());

        return newReview;
    }

    /**
     * Deletes a review from the system and unlinks it from the author.
     *
     * @param reviewId the ID of the review to delete
     */
    public void deleteReview(long reviewId) {
        Review review = findReviewById(reviewId);

        userService.unlinkReviewFromUser(review.getAuthor().getId(), reviewId);

        reviewRepository.delete(review);
    }

    private void validateReviewInput(String content, int ratingValue) {
        if (content == null) {
            throw new ParametersException("Content is null");
        } else if (content.length() > maxCommentRange) {
            throw new InvalidCommentRangeException(
                    String.format("Review content is limited to %d current : %d", maxCommentRange, content.length())
            );
        }

        if (ratingValue > maxRating || ratingValue < minRating) {
            throw new InvalidRatingScoreException(
                    String.format("Rating must be between %d and %d current : %d", maxCommentRange, minRating, ratingValue)
            );
        }
    }
}