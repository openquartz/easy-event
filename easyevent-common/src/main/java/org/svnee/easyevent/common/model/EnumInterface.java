package org.svnee.easyevent.common.model;

/**
 * EnumInterface
 *
 * @author svnee
 */
public interface EnumInterface<K> {

    /**
     * code
     *
     * @return code
     */
    K getCode();

    /**
     * desc
     *
     * @return desc
     */
    String getDesc();
}
