package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;

public record BoardGameItemDTO(Long id, String serialNumber, BoardGameState state, String name) implements BasicDTO {
public String getName(){
    return name;
}
}
