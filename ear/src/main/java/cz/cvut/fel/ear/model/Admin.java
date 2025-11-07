package cz.cvut.fel.ear.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin entity representing administrative users in the system.
 * Inherits from the User class.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@DiscriminatorValue("ADMIN")
public class Admin extends User {
}
