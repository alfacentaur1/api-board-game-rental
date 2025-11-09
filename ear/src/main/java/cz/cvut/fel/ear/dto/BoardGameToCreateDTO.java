package cz.cvut.fel.ear.dto;

public record BoardGameToCreateDTO(String name, String description) {
    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }
}
