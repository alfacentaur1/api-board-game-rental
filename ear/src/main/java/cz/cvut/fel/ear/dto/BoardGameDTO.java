package cz.cvut.fel.ear.dto;

public record BoardGameDTO(Long id, int availableCopies, String description, String name) implements BasicDTO{
}
