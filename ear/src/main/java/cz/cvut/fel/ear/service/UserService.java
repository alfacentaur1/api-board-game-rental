package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.UserRepository;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.exception.EntityAlreadyExistsException;
import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.*;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LoanService loanService;
    private final ReviewService reviewService;
    private final PasswordEncoder passwordEncoder;

    public UserService(LoanService loanService, ReviewService reviewService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.loanService = loanService;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user
     * @return the User entity
     * @throws EntityNotFoundException if the user is not found
     */
    public User findById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new EntityNotFoundException(User.class.getSimpleName(), userId)
                );
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the User entity
     * @throws EntityNotFoundException if the user is not found
     */
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new EntityNotFoundException(User.class.getSimpleName(), null);
        }

        return user;
    }

    /**
     * Associates a loan with a specific user.
     *
     * @param userId the ID of the user
     * @param loanId the ID of the loan
     */
    public void linkLoanToUser(long userId, long loanId) {
        BoardGameLoan loanToAdd = loanService.getBoardGameLoan(loanId);
        RegisteredUser user = (RegisteredUser) findById(userId);

        user.getBoardGameLoans().add(loanToAdd);
        userRepository.save(user);
    }

    /**
     * Associates a review with a specific user.
     *
     * @param userId   the ID of the user
     * @param reviewId the ID of the review
     */
    public void linkReviewToUser(long userId, long reviewId) {
        Review reviewToAdd = reviewService.findReviewById(reviewId);
        RegisteredUser user = (RegisteredUser) findById(userId);

        user.getRatings().add(reviewToAdd);
        userRepository.save(user);
    }

    /**
     * Removes the association between a review and a user.
     *
     * @param userId   the ID of the user
     * @param reviewId the ID of the review
     * @throws EntityNotFoundException if the review is not linked to the user
     */
    public void unlinkReviewFromUser(long userId, long reviewId) {
        Review reviewToRemove = reviewService.findReviewById(reviewId);
        RegisteredUser user = (RegisteredUser) findById(userId);

        if (!user.getRatings().contains(reviewToRemove)) {
            throw new EntityNotFoundException(User.class.getSimpleName(), userId, Review.class.getSimpleName(), reviewId);
        }

        user.getRatings().remove(reviewToRemove);
        userRepository.save(user);
    }

    /**
     * Registers a new regular user in the system.
     *
     * @param registrationDTO DTO containing registration details
     * @throws EntityAlreadyExistsException if a user with the same username already exists
     */
    @Transactional
    public void registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.findByUsername(registrationDTO.username()) != null) {
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

    /**
     * Registers a new administrator in the system.
     *
     * @param registrationDTO DTO containing registration details
     * @throws EntityAlreadyExistsException if a user with the same username already exists
     */
    @Transactional
    public void registerAdmin(UserRegistrationDTO registrationDTO) {
        if (userRepository.findByUsername(registrationDTO.username()) != null) {
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

    /**
     * Updates user karma based on loan return punctuality.
     * Deducts 5 karma points for late returns, adds 10 for on-time returns.
     *
     * @param userId the ID of the user
     * @param isLate whether the loan was returned late
     */
    public void updateKarmaForLoanReturn(long userId, boolean isLate) {
        RegisteredUser user = (RegisteredUser) findById(userId);

        if (isLate) {
            if (user.getKarma() > 4) {
                user.setKarma(user.getKarma() - 5);
            }
        } else {
            if (user.getKarma() < 91) {
                user.setKarma(user.getKarma() + 10);
            }
        }

        userRepository.save(user);
    }
}