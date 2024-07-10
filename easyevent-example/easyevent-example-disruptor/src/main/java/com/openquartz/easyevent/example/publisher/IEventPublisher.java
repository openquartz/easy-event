package com.openquartz.easyevent.example.publisher;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.openquartz.easyevent.core.IEvent;
import org.springframework.stereotype.Component;
import com.openquartz.easyevent.core.publisher.EventPublisher;

/**
 * @author svnee
 **/
@Component
public class IEventPublisher {

    @Resource
    private EventPublisher eventPublisher;

    public <T extends IEvent>void publish(T event) {
        eventPublisher.syncPublish(event);
    }

    public <T extends IEvent>void asyncPublish(T event) {
        eventPublisher.asyncPublish(event);
    }

    public <T extends IEvent>void asyncPublishList(List<T> eventList) {
        ArrayList<Object> list = new ArrayList<>(eventList);
        eventPublisher.asyncPublishList(list);
    }

}
