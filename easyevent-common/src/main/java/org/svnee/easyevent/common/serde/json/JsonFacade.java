package org.svnee.easyevent.common.serde.json;

import java.util.List;
import java.util.Set;

/**
 * Json facade.
 *
 * @author svnee
 */
public interface JsonFacade {

    /**
     * To JSON string.
     *
     * @param object object
     * @return string
     */
    String toJson(Object object);

    /**
     * Parse object.
     *
     * @param text text
     * @param clazz clazz
     * @param <T> T
     * @return T
     */
    <T> T parseObject(String text, Class<T> clazz);

    /**
     * 序列化
     *
     * @param json json
     * @param type type
     * @param <T> T
     * @return T
     */
    <T> T parseObject(byte[] json, Class<T> type);

    /**
     * Type
     *
     * @param text text
     * @param typeReference type
     * @param <T> T
     * @return T
     */
    <T> T parseObject(String text, TypeReference<T> typeReference);

    /**
     * Type
     *
     * @param json text
     * @param typeReference type
     * @param <T> T
     * @return T
     */
    <T> T parseObject(byte[] json, TypeReference<T> typeReference);

    /**
     * Parse array.
     *
     * @param text text
     * @param clazz clazz
     * @param <T> T
     * @return T
     */
    <T> List<T> parseArray(String text, Class<T> clazz);

    /**
     * Parse Set
     *
     * @param json json
     * @param clazz clazz
     * @param <T> T
     * @return Set
     */
    <T> Set<T> parseSet(String json, Class<T> clazz);

    /**
     * TO byte
     *
     * @param obj obj
     * @return obj
     */
    byte[] toJsonAsBytes(Object obj);
}
