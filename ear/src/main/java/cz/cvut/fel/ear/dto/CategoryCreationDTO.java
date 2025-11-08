package cz.cvut.fel.ear.dto;

public record CategoryCreationDTO(String name) {

    public String getName() {
        return name;
    }
}
