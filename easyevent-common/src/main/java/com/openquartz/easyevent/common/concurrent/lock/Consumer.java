package com.openquartz.easyevent.common.concurrent.lock;

/**
 * consumer function
 *
 * @author svnee
 */
@FunctionalInterface
public interface Consumer {

    /**
     * consume
     */
    void consume();
}
