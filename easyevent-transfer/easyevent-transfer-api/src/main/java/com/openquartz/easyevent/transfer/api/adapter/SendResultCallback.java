package com.openquartz.easyevent.transfer.api.adapter;

import com.openquartz.easyevent.storage.identify.EventId;

/**
 * send result call back
 *
 * @author svnee
 */
public interface SendResultCallback {

    /**
     * on success
     */
    void onSuccess(EventId eventId);

    /**
     * on fail-message
     *
     * @param error error
     */
    void onFail(EventId eventId, Throwable error);
}
