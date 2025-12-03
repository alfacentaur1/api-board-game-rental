package cz.cvut.fel.ear.service;


import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.*;
import io.jsonwebtoken.security.Password;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LoanService loanService;
    private final ReviewService reviewService;
    private final PasswordEncoder passwordEncoder;

    public UserService( LoanService loanService, ReviewService reviewService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.loanService = loanService;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(User.class.getSimpleName(), userId)
                );
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new EntityNotFoundException(User.class.getSimpleName(), null);
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
            throw new EntityNotFoundException(User.class.getSimpleName(), userId, Review.class.getSimpleName(), reviewId);
        }

        // Remove it
        user.getRatings().remove(reviewToRemove);
        userRepository.save(user);
    }


    @Transactional
    public void registerUser(UserRegistrationDTO registrationDTO) {
        if(userRepository.findByUsername(registrationDTO.username()) != null) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName(), registrationDTO.username());
        }
        RegisteredUser newUser = new RegisteredUser();
        newUser.setUsername(registrationDTO.username());
        String encodedPassword = passwordEncoder.encode(registrationDTO.password());
        newUser.setPassword(encodedPassword);
        newUser.setEmail(registrationDTO.email());
        newUser.setFullName(registrationDTO.fullName());
        userRepository.save(newUser);
    }

    @Transactional
    public void registerAdmin(UserRegistrationDTO registrationDTO) {
        if(userRepository.findByUsername(registrationDTO.username()) != null) {
            throw new EntityAlreadyExistsException(User.class.getSimpleName(), registrationDTO.username());
        }
        Admin newAdmin = new Admin();
        newAdmin.setUsername(registrationDTO.username());
        String encodedPassword = passwordEncoder.encode(registrationDTO.password());
        newAdmin.setPassword(encodedPassword);
        newAdmin.setEmail(registrationDTO.email());
        newAdmin.setFullName(registrationDTO.fullName());
        userRepository.save(newAdmin);
    }


}
