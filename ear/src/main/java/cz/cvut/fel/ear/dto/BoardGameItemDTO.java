package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;

public record BoardGameItemDTO(Long id, String serialNumber, BoardGameState state, String name) {

    public Long getId(){
        return id;
    }

    public String getSerialNumber(){
        return serialNumber;
    }

    public BoardGameState getState(){
        return state;
    }

    public String getName(){
        return name;
    }
}
