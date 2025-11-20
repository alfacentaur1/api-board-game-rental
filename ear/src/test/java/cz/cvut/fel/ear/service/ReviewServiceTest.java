package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.exception.InvalidCommentRangeException;
import cz.cvut.fel.ear.exception.InvalidRatingScoreException;
import cz.cvut.fel.ear.exception.ParametersException;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.model.Review;
import cz.cvut.fel.ear.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model")
@ActiveProfiles("test")
public class ReviewServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReviewService sut;

    @MockitoSpyBean
    private UserService userService;

    private RegisteredUser testUser;
    private BoardGame testGame;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // Set up user
        testUser = new RegisteredUser();
        testUser.setUsername("JohnDoe");
        testUser.setEmail("test@test.com");
        testUser.setFullName("John Doe");
        em.persist(testUser);

        // Set up game
        testGame = new BoardGame();
        testGame.setName("Game1");
        testGame.setDescription("description for board game");
        em.persist(testGame);

        // Set up review
        testReview = new Review();
        testReview.setAuthor(testUser);
        testReview.setBoardGame(testGame);
        testReview.setComment("Amazing game!");
        testReview.setScore(5);
        em.persist(testReview);


        // Link review to user
        testUser.getRatings().add(testReview);

        em.flush();
    }

    @Test
    @DisplayName("retrieve a review by id and verify exception when missing")
    void testFindReviewById() {
        Review foundReview = sut.findReviewById(testReview.getId());

        // Check if review was found
        assertNotNull(foundReview);
        assertEquals(testReview.getId(), foundReview.getId());

        // Check if correct exception is thrown when review is not found
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.findReviewById(-1)
        );
    }

    @Test
    @DisplayName("get all reviews for a board game and return empty for none")
    void testGetReviewsForBoardGame() {
        List<Review> reviews = sut.getReviewsForBoardGame(testGame.getId());
        assertFalse(reviews.isEmpty());
        assertTrue(reviews.stream().anyMatch(r -> r.getId() == testReview.getId()));

        // Check for game with no reviews
        BoardGame newGame = new BoardGame();
        newGame.setName("EmptyGame");
        newGame.setDescription("No reviews yet");
        em.persist(newGame);
        em.flush();

        List<Review> emptyReviews = sut.getReviewsForBoardGame(newGame.getId());
        assertTrue(emptyReviews.isEmpty());
    }

    @Test
    @DisplayName("create a review and ensure it is persisted and linked")
    void testCreateReview_successful() {
        String content = "New game review content";
        int ratingValue = 4;

        // Create new review
        Review createdReview = sut.createReview(testUser.getId(), testGame.getId(), content, ratingValue);
        em.flush();

        Review foundReview = em.find(Review.class, createdReview.getId());

        // Check review was created successfully
        assertNotNull(foundReview);
        assertEquals(content, foundReview.getComment());
        assertEquals(ratingValue, foundReview.getScore());
        assertEquals(testUser.getId(), foundReview.getAuthor().getId());
        assertEquals(testGame.getId(), foundReview.getBoardGame().getId());

        // Check that user has review linked to him
        assertTrue(testUser.getRatings().contains(foundReview));
    }

    @Test
    @DisplayName("validate parameters when creating a review and expect exceptions")
    void testCreateReview_invalidParameters() {
        // Check if correct exception is thrown when incorrect content is given
        assertThrows(
                ParametersException.class,
                () -> sut.createReview(testUser.getId(), testGame.getId(), null, 3)
        );

        String exact201 = "A".repeat(201);
        assertThrows(
                InvalidCommentRangeException.class,
                () -> sut.createReview(testUser.getId(), testGame.getId(), exact201, 3)
        );

        // Check if correct exception is thrown when incorrect rating value is given
        assertThrows(
                InvalidRatingScoreException.class,
                () -> sut.createReview(testUser.getId(), testGame.getId(), "TEST CONTENT", 6)
        );

        // Check if correct exception is thrown when incorrect userid is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.createReview(-1L, testGame.getId(), "Test review", 4)
        );

        // Check if correct exception is thrown when incorrect gameid is given
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.createReview(testUser.getId(), -99L, "Test review", 4)
        );
    }

    @Test
    @DisplayName("delete a review and ensure it is removed from the database")
    void testDeleteReview() {
        sut.deleteReview(testReview.getId());
        em.flush();

        // Check that review was deleted
        assertNull(em.find(Review.class, testReview.getId()));

        // Check if correct exception is thrown for non-existing review
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.deleteReview(-1)
        );
    }


    // Business tests

    @Test
    @DisplayName("creating review for the same game increases the review count")
    void testMoreReviewsForSameGame() {
        // Create new review
        sut.createReview(testUser.getId(), testGame.getId(), "Second review content", 4);
        em.flush();

        // Check if both reviews are saved
        List<Review> foundReviews = sut.getReviewsForBoardGame(testGame.getId());
        assertEquals(2, foundReviews.size());
    }

    @Test
    @DisplayName("deleting a review removes the link from the user and the review cannot be found")
    void testDeletingReviewUnlinksFromUser() {
        // Check that user has linked review
        User foundUser = userService.findById(testUser.getId());

        long reviewId = foundUser.getRatings().getFirst().getId();
        Review foundReview = sut.findReviewById(reviewId);

        assertEquals(foundUser.getId(), foundReview.getAuthor().getId());

        // Delete the review
        sut.deleteReview(foundReview.getId());

        // Check that review cant be found
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.findReviewById(foundReview.getId())
        );

        // Check that user doesnt have the link to the review
        User newUser = userService.findById(testUser.getId());

        assertFalse(newUser.getRatings().contains(foundReview));
    }
}
