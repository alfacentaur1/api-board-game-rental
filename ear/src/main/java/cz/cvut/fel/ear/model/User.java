package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "USER_TYPE")
@Table(name = "users_table")
@Getter
public abstract class User {
    @Id
    @GeneratedValue
    protected long id;

    @Column(unique = true)
    protected String username;

    protected String password;

    @Column(unique = true)
    protected String email;

    protected String fullName;

    //mapping composition here
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    protected List<Review> ratings = new ArrayList<>();

    public abstract UserRole getRole();

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

    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
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
