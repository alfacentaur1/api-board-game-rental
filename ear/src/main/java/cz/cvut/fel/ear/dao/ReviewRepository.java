package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Finds all reviews for a given board game
     * @param gameId id of the board game to find reviews for
     * @return list of reviews belonging to the given board game
     */
    List<Review>findAllByBoardGame(long gameId);
}
