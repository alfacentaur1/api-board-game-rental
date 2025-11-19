package cz.cvut.fel.ear.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
