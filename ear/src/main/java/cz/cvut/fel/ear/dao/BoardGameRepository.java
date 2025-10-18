package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Integer> {
    public int findBoardGameIdByGameName(String gameName);

}