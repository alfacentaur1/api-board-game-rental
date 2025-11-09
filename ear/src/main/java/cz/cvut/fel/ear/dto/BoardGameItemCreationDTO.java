package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;

public record BoardGameItemCreationDTO(Long boardGameId, String serialNumber, BoardGameState state) {
    public String getSerialNumber() {
        return serialNumber;
    }

    public BoardGameState getState() {
        return state;
    }

    public Long getBoardGameId() {
        return boardGameId;
    }
}
