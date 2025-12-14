package cz.cvut.fel.ear.mapper;

import cz.cvut.fel.ear.dto.BoardGameItemDTO;
import cz.cvut.fel.ear.model.BoardGameItem;
import org.springframework.stereotype.Component;

@Component
public class BoardGameItemMapper {

    public BoardGameItemDTO toDto(BoardGameItem item) {
        if (item == null) {
            return null;
        }

        String gameName = (item.getBoardGame() != null) ? item.getBoardGame().getName() : null;

        return new BoardGameItemDTO(
                item.getId(),
                item.getSerialNumber(),
                item.getState(),
                gameName
        );
    }
}