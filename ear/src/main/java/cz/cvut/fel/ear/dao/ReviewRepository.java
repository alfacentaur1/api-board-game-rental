package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // TODO - add comment
    List<Review> findAllByBoardGame_IdIn(List<Long> integers);

}
