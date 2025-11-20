package cz.cvut.fel.ear.service;


import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LoanService loanService;
    private final ReviewService reviewService;

    public UserService(UserRepository userRepository, LoanService loanService, ReviewService reviewService) {
        this.userRepository = userRepository;
        this.loanService = loanService;
        this.reviewService = reviewService;
    }

    public User findById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format("Registered user with id %d not found", userId)
                        )
                );
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new EntityNotFoundException(
                    String.format("Registered user with id %s not found", username)
            );
        }

        return user;
    }

    public void linkLoanToUser(long userId, long loanId) {
        BoardGameLoan loanToAdd = loanService.getBoardGameLoan(loanId);
        RegisteredUser user = (RegisteredUser)findById(userId);

        user.getBoardGameLoans().add(loanToAdd);
        userRepository.save(user);
    }

    public void linkReviewToUser(long userId, long reviewId) {
        Review reviewToAdd = reviewService.findReviewById(reviewId);
        RegisteredUser user = (RegisteredUser)findById(userId);

        user.getRatings().add(reviewToAdd);
        userRepository.save(user);
    }

    public void unlinkReviewFromUser(long userId, long reviewId) {
        Review reviewToRemove = reviewService.findReviewById(reviewId);
        RegisteredUser user = (RegisteredUser)findById(userId);

        // Check that user has this review linked to him
        if (!user.getRatings().contains(reviewToRemove)) {
            throw new EntityNotFoundException(
                    String.format("User %d doesn't have review %d linked to him", userId, reviewId)
            );
        }

        // Remove it
        user.getRatings().remove(reviewToRemove);
        userRepository.save(user);
    }

    public void deleteUser(long userId) {
        if(!loanService.getAllBoardGameLoansByUser(userId).isEmpty()) {
            throw new IllegalStateException("Cannot delete user with active loans");
        }
        RegisteredUser user = (RegisteredUser)findById(userId);
        userRepository.delete(user);
    }


    @Transactional
    public void registerUser(UserRegistrationDTO registrationDTO) {
        if(userRepository.findByUsername(registrationDTO.username()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        RegisteredUser newUser = new RegisteredUser();
        newUser.setUsername(registrationDTO.username());
        newUser.setPassword(registrationDTO.password());
        newUser.setEmail(registrationDTO.email());
        newUser.setFullName(registrationDTO.fullName());
        userRepository.save(newUser);
    }

    @Transactional
    public void registerAdmin(UserRegistrationDTO registrationDTO) {
        if(userRepository.findByUsername(registrationDTO.username()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        Admin newAdmin = new Admin();
        newAdmin.setUsername(registrationDTO.username());
        newAdmin.setPassword(registrationDTO.password());
        newAdmin.setEmail(registrationDTO.email());
        newAdmin.setFullName(registrationDTO.fullName());
        userRepository.save(newAdmin);
    }


}
