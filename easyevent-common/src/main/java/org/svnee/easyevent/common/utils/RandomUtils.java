package org.svnee.easyevent.common.utils;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.security.SecureRandom;
import java.util.Random;

/**
 * RandomUtils
 *
 * @author svnee
 **/
public final class RandomUtils {

    private RandomUtils() {
    }

    private static Random rand = null;

    static {
        try {
            rand = SecureRandom.getInstanceStrong();
        } catch (Exception ex) {
            ExceptionUtils.rethrow(ex);
        }
    }

    /**
     * return [0,bound)
     *
     * @param bound bound value
     * @return random value
     */
    public static Integer nextInt(Integer bound) {

        checkNotNull(bound);
        checkNotNull(rand);

        return rand.nextInt(bound);
    }

}
