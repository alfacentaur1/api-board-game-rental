package cz.cvut.fel.ear.exception;

public class EntityNotFoundException extends RuntimeException {
    private String item = null;
    private String resouce = null;

    public EntityNotFoundException(String item, Long itemId) {
        super("%s with id %s not found".formatted(item, itemId));
        this.item = item;
    }

    public EntityNotFoundException(String item, Long itemId, String resource, Long resourceId) {
        super("%s with id %s not found in %s with id %s".formatted(item, itemId, resource, resourceId));
        this.item = item;
        this.resouce = resource;
    }

    public String getItem() {
        return item;
    }

    public String getResouce() {
        return resouce;
    }
}
