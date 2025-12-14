package cz.cvut.fel.ear.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    private String item = null;
    private String resouce = null;

    public EntityAlreadyExistsException(String item, Object itemId) {
        super("%s with id %s not found".formatted(item, itemId));
        this.item = item;
    }

    public EntityAlreadyExistsException(String item, Object itemId, String resource, Object resourceId) {
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
