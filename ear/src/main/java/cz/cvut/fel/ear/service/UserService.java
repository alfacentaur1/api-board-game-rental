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

        user.getBoardGameLoans().add(loanToAdd);
        registeredUserRepository.save(user);
    }

    public void linkReviewToUser(long userId, long loanId) {
        Review reviewToAdd = reviewService.findReviewById(loanId);
        RegisteredUser user = findById(userId);

        user.getRatings().add(reviewToAdd);
        registeredUserRepository.save(user);
    }

}
