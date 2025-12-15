package cz.cvut.fel.ear.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    @Override
    public UserRole getRole() {
        return UserRole.ROLE_ADMIN;
    }
}
