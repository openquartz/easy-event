package org.svnee.easyevent.storage.jdbc.utils;

/**
 * class type default value
 *
 * @author svnee
 **/
public class TypeDefaultVal<T> {

    private final Class<T> type;
    private final T value;
    private Integer order = Integer.MAX_VALUE;

    public TypeDefaultVal(Class<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public TypeDefaultVal(Class<T> type, T value, Integer order) {
        this.type = type;
        this.value = value;
        this.order = order;
    }

    public Class<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public Integer getOrder() {
        return order;
    }
}
