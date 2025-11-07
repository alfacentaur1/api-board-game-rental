package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameRepository;
import cz.cvut.fel.ear.dao.ReviewRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidRatingScoreException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.model.Review;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BoardGameRepository boardGameRepository;
    private final int maxRating = 5;
    private final int minRating = 5;
    private final int maxCommentRange = 200;

    public ReviewService(ReviewRepository reviewRepository, BoardGameRepository boardGameRepository) {
        this.reviewRepository = reviewRepository;
        this.boardGameRepository = boardGameRepository;
    }

    public List<Review> getAllBoardGameReviewsById(int gameId) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(gameId);
        if (boardGame == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        return reviewRepository.findAllByBoardGame_IdIn(Collections.singletonList(gameId));
    }


    @Transactional
    public Review createReview(long gameId, String content, int rating, RegisteredUser registeredUser) {
        BoardGame boardGame = boardGameRepository.getBoardGameById(gameId);
        if (boardGame == null) {
            throw new EntityNotFoundException("Board game with id " + gameId + " not found");
        }
        if (content == null) {
            throw new ParametersException("content is null");
        }
        if(registeredUser == null ){
            throw new ParametersException("registered user is null");
        }

        if (rating > maxRating || rating < minRating) {
            throw new InvalidRatingScoreException("Rating must be between " + minRating + " and " + maxRating);
        }

        if (content.length() > maxCommentRange) {
            throw new InvalidRatingScoreException("Review content is limited to " + maxCommentRange + " characters");
        }

        Review review = new Review();
        review.setBoardGame(boardGame);
        review.setComment(content);
        review.setScore(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setAuthor(registeredUser);

        reviewRepository.save(review);
        return review;
    }


    public void deleteReview(long id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            throw new EntityNotFoundException("Review with id " + id + " not found");
        }
        reviewRepository.delete(review);

    }

}
