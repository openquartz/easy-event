package org.svnee.easyevent.core.trigger;

import org.svnee.easyevent.transfer.api.message.EventMessage;

/**
 * 事件触发器
 *
 * @author svnee
 */
public interface AsyncEventHandler {

    /**
     * 触发消息
     *
     * @param eventMessage 事件消息
     */
    void handle(EventMessage eventMessage);

}
