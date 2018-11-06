package com.expedia.adaptivealerting.modelservice.repo;

public class ItemExistsException extends RuntimeException {
    private Object existingItem;

    public ItemExistsException(Object existingItem) {
        this.existingItem = existingItem;
    }

    public Object getExistingItem() {
        return existingItem;
    }
}
