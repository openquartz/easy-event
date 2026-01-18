package com.openquartz.easyevent.storage.model;

import java.util.List;
import lombok.Data;
import com.openquartz.easyevent.storage.identify.EventId;

/**
 * BaseEvent
 *
 * @author svnee
 **/
@Data
public class BaseEventEntity {

    /**
     * eventId
     */
    private Long entityId;

    /**
     * appId
     */
    private String appId;

    /**
     * sourceId
     */
    private Long sourceId;

    /**
     * traceId
     */
    private String traceId;

    /**
     * Event-Class
     */
    private String className;

    /**
     * 执行错误次数
     */
    private Integer errorCount;

    /**
     * 成功的处理订阅者
     */
    private List<String> successfulSubscriberList;

    /**
     * 执行状态
     */
    private EventLifecycleState processingState;

    /**
     * 是否处理完成
     *
     * @return 是否处理完成
     */
    public boolean isProcessComplete() {
        return processingState == EventLifecycleState.PROCESS_COMPLETE;
    }

    public EventId getEventId() {
        return new EventId(entityId, sourceId);
    }
}
