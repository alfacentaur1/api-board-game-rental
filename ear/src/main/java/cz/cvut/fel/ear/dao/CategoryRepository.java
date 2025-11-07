package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * Finds a category by its name
     * @param name name of the category
     * @return optional containing the category if found, empty otherwise
     */
    Optional<Category> findByName(String name);
}
