package com.openquartz.easyevent.common.utils;

import java.util.Arrays;

/**
 * ObjectUtils
 *
 * @author svnee
 **/
public final class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * hash code
     *
     * @param obj obj
     * @return hash code
     */
    public static int hashCode(Object... obj) {
        return Arrays.hashCode(obj);
    }
}
