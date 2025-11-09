package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;

public record BoardGameItemDTO(Long id, String serialNumber, BoardGameState state) {

    public Long getId(){
        return id;
    }

    public String getSerialNumber(){
        return serialNumber;
    }

    public BoardGameState getState(){
        return state;
    }
}
