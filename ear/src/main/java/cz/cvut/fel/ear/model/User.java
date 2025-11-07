package cz.cvut.fel.ear.model;

import jakarta.persistence.*;

import java.util.List;

/**
 * Abstract User entity representing a user in the system.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "USER_TYPE")
@Table(name = "users_table")
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
    @Column(unique = true)
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
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    protected List<Review> ratings;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Review> getRatings() {
        return ratings;
    }

    public void setRatings(List<Review> ratings) {
        this.ratings = ratings;
    }

}
