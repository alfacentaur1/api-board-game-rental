package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.RegisteredUserRepository;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.model.Review;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final RegisteredUserRepository registeredUserRepository;
    private final LoanService loanService;
    private final ReviewService reviewService;

    public UserService(RegisteredUserRepository registeredUserRepository, LoanService loanService, ReviewService reviewService) {
        this.registeredUserRepository = registeredUserRepository;
        this.loanService = loanService;
        this.reviewService = reviewService;
    }

    public RegisteredUser findById(long userId) {
        return registeredUserRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format("Registered user with id %d not found", userId)
                        )
                );
    }

    // TODO - implement for admin too?
    public RegisteredUser getRegisteredUserByUsername(String username) {
        RegisteredUser user = registeredUserRepository.findByUsername(username);

        if (user == null) {
            throw new EntityNotFoundException(
                    String.format("Registered user with id %s not found", username)
            );
        }

        return user;
    }

    public void linkLoanToUser(long userId, long loanId) {
        BoardGameLoan loanToAdd = loanService.getBoardGameLoan(loanId);
        RegisteredUser user = findById(userId);

        // TODO - implement for admin -> update whose repository to use
        user.getBoardGameLoans().add(loanToAdd);
        registeredUserRepository.save(user);
    }

    public void linkReviewToUser(long userId, long reviewId) {
        Review reviewToAdd = reviewService.findReviewById(reviewId);
        RegisteredUser user = findById(userId);

        // TODO - implement for admin -> update whose repository to use
        user.getRatings().add(reviewToAdd);
        registeredUserRepository.save(user);
    }

    public void unlinkReviewFromUser(long userId, long reviewId) {
        Review reviewToRemove = reviewService.findReviewById(reviewId);
        RegisteredUser user = findById(userId);

        // TODO - implement for admin -> update whose repository to use
        // Check that user has this review linked to him
        if (!user.getRatings().contains(reviewToRemove)) {
            throw new EntityNotFoundException(
                    String.format("User %d doesn't have review %d linked to him", userId, reviewId)
            );
        }

        // Remove it
        user.getRatings().remove(reviewToRemove);
        registeredUserRepository.save(user);
    }

    public void createUser(RegisteredUser user) {
        if(registeredUserRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        registeredUserRepository.save(user);
    }

    public void deleteUser(long userId) {
        if(!loanService.getAllBoardGameLoansByUser(userId).isEmpty()) {
            throw new IllegalStateException("Cannot delete user with active loans");
        }
        RegisteredUser user = findById(userId);
        registeredUserRepository.delete(user);
    }

    //TODO login, logout, update user details

}
