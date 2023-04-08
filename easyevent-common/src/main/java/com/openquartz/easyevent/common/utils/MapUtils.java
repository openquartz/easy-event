package com.openquartz.easyevent.common.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 集合工具类
 *
 * @author svnee
 **/
public final class MapUtils {

    private MapUtils() {
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return Objects.isNull(map) || map.isEmpty();
    }

    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    public static <K, V> Map<K, V> newHashMapWithExpectedSize(int expectedSize) {
        return new HashMap<>(calCapacity(expectedSize));
    }

    /**
     * 计算期望容量
     *
     * @param expectedSize 期望容量
     * @return 返回实际期望容量
     */
    private static int calCapacity(int expectedSize) {
        if (expectedSize < 3) {
            checkNonnegative(expectedSize, "expectedSize");
            return expectedSize + 1;
        } else {
            return expectedSize < 1073741824 ? (int) (expectedSize / 0.75F + 1.0F) : 2147483647;
        }
    }

    private static int checkNonnegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
        } else {
            return value;
        }
    }

    /**
     * new HashMap
     *
     * @return Map
     */
    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * new linkedHashMap
     *
     * @return linkedHashMap
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * copy map
     */
    public static <K, V> Map<K, V> copyOf(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        if (map.isEmpty()) {
            return new HashMap<>();
        }
        Map<K, V> result = new LinkedHashMap<>(map.size());
        result.putAll(map);
        return result;
    }
}
