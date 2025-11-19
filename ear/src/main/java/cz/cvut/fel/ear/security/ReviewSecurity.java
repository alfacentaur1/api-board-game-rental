package cz.cvut.fel.ear.security;

import cz.cvut.fel.ear.dao.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("reviewSecurity")
public class ReviewSecurity {

    @Autowired
    private ReviewRepository reviewRepository;

    public boolean isOwner(Long reviewId, Authentication authentication) {
        var review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return false;

        String currentUsername = authentication.getName();
        return review.getAuthorAsRegisteredUser().getUsername().equals(currentUsername);
    }
}
