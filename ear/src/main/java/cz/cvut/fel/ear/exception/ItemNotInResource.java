package cz.cvut.fel.ear.exception;

public class ItemNotInResource extends RuntimeException {

    private String item;
    private String resource;

    public ItemNotInResource(String item, String resource) {
        super("%s not in %s".formatted(item, resource));
        this.item = item;
        this.resource = resource;
    }

    public String getItem() {
        return item;
    }

    public String getResource() {
        return resource;
    }
}
