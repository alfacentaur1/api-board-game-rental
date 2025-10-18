package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//annotation for spring - persistence layer, managed by spring
@Repository
public interface BoardGameItemRepository extends JpaRepository<BoardGameItem, Integer> {
}
