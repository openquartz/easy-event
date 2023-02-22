package org.svnee.easyevent.core.publisher;

import java.util.List;

/**
 * @author svnee
 **/
public interface EventPublisher {

    /**
     * 同步发布
     *
     * @param event event 同步消息
     * @param <T> 消息类型
     * @return 发送结果。是否全部执行成功
     */
    <T> boolean syncPublish(T event);

    /**
     * 异步发布
     *
     * @param event event
     * @param <T> 消息类型
     * @return 发送结果
     */
    <T> boolean asyncPublish(T event);

    /**
     * 批量发布消息
     *
     * @param eventList 发布事件
     * @return 是否发布成功
     */
    boolean asyncPublishList(List<Object> eventList);

}
