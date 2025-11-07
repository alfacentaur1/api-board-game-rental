package cz.cvut.fel.ear.service.interfaces;

import cz.cvut.fel.ear.model.Review;
import cz.cvut.fel.ear.model.User;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ReviewServiceI {

    /**
     * Get all reviews for a board game based on its id
     * @param gameId id of the board game to get the reviews for
     * @return list of reviews
     */
    List<Review> getBoardGameReviews(long gameId);

     @Transactional
     void createReview(User user, long gameId, String content, Integer rating);

    /**
     * Updates review
     * null for no change
     * @param id id of the review to update
     * @param content new content for the review
     * @param rating new rating for the review
     */
     @Transactional
     void updateReview(long id, String content, Integer rating);

     /**
     * Deletes review
     * @param id id of the review to delete
     */
    @Transactional
    void deleteReview(long id);

}
