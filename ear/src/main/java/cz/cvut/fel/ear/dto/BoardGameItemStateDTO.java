package cz.cvut.fel.ear.dto;

import cz.cvut.fel.ear.model.BoardGameState;

import java.io.Serializable;

public record BoardGameItemStateDTO(BoardGameState boardGameState, Long id) {
}
