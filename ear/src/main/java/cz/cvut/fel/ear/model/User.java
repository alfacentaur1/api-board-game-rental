package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

/**
 * Abstract User entity representing a user in the system.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "USER_TYPE")
@Getter
public abstract class User {
    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue
    protected long id;

    /**
     * Username of the user.
     */
    @Column(unique=true)
    protected String username;

    /**
     * Email address of the user.
     */
    @Column(unique = true)
    protected String email;

    /**
     * Full name of the user.
     */
    protected String fullName;

    //mapping composition here
    /**
     * List of reviews authored by the user.
     */
    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    protected List<Review> ratings;


}
