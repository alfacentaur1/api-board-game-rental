package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.exception.EntityNotFoundException;
import cz.cvut.fel.ear.model.RegisteredUser;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan("cz.cvut.fel.ear.model")
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserService sut;

    @MockitoSpyBean
    private LoanService loanService;

    private RegisteredUser testUser;

    @BeforeEach
    void setUp() {
        // SetUp user
        testUser = new RegisteredUser();
        testUser.setUsername("JohnDoe");
        testUser.setEmail("test@test.com");
        testUser.setFullName("John Doe");
        em.persist(testUser);
    }


    @Test
    @DisplayName("Should find a user by id and throw when not found")
    void testFindById() {
        RegisteredUser userFound = (RegisteredUser)sut.findById(testUser.getId());

        // Check if user was found
        assertNotNull(userFound);
        assertEquals(testUser.getId(), userFound.getId());
        assertEquals(testUser.getUsername(), userFound.getUsername());

        // Check if correct exception is thrown when user is not found
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.findById(-1)
        );
    }

    @Test
    @DisplayName("Should get a registered user by username and throw when invalid")
    void testGetRegisteredUserByUsername() {
        User userFound = sut.getUserByUsername(testUser.getUsername());

        // Check if user was found
        assertNotNull(userFound);
        assertEquals(testUser.getId(), userFound.getId());
        assertEquals(testUser.getUsername(), userFound.getUsername());

        // Check if correct exception is thrown when user is not found
        assertThrows(
                EntityNotFoundException.class,
                () -> sut.getUserByUsername(null)
        );
    }
}
