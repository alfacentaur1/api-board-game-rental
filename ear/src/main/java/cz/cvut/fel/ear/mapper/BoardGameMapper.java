package cz.cvut.fel.ear.mapper;

import cz.cvut.fel.ear.dto.BoardGameDTO;
import cz.cvut.fel.ear.model.BoardGame;
import org.springframework.stereotype.Component;

@Component
public class BoardGameMapper {

    public BoardGameDTO toDto(BoardGame boardGame) {
        if (boardGame == null) {
            return null;
        }
        return new BoardGameDTO(
                boardGame.getId(),
                boardGame.getAvailableCopies(),
                boardGame.getDescription(),
                boardGame.getName()
        );
    }
}