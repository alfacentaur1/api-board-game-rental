package cz.cvut.fel.ear.mapper;

import cz.cvut.fel.ear.dto.ReviewDetailDTO;
import cz.cvut.fel.ear.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewDetailDTO toReviewDetailDTO(Review review) {
        return new ReviewDetailDTO(
                review.getScore(),
                review.getComment(),
                review.getAuthor().getUsername(),
                review.getBoardGame().getName(),
                review.getCreatedAt());
    }

}
