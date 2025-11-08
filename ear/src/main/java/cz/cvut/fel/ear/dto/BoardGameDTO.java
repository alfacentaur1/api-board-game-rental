package cz.cvut.fel.ear.dto;

public record BoardGameDTO(Long id, int availableCopies, String description, String name) {

    public Long getId() {
        return id;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public String getDescription() {
        return description;
    }
    public String getName() {
        return name;
    }
}
