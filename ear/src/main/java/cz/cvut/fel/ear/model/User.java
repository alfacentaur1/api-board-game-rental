package cz.cvut.fel.ear.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "USER_TYPE")
public abstract class User {
    @Id
    @GeneratedValue
    protected int id;

    @Column(unique=true)
    protected String username;

    @Column(unique = true)
    protected String email;

    protected String fullName;

    //mapping composition here
    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    protected List<Rating> ratings;


}
