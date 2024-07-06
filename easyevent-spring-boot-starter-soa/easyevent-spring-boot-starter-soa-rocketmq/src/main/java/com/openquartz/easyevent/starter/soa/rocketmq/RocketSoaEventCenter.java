package com.openquartz.easyevent.starter.soa.rocketmq;

import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.Subscriber;
import com.openquartz.easyevent.core.expression.ExpressionParser;
import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.starter.soa.api.SoaEvent;
import com.openquartz.easyevent.starter.soa.core.SoaEventCenter;

import java.util.Iterator;
import java.util.List;

/**
 * RocketMqEventCenter
 *
 * @author svnee
 */
public class RocketSoaEventCenter implements SoaEventCenter {

    private final List<EventBus> eventBusList;
    private final ExpressionParser expressionParser;
    private final EventPublisher eventPublisher;
    private final SoaEventRocketMqProducer soaEventRocketMqProducer;

    public RocketSoaEventCenter(List<EventBus> eventBusList,
                                ExpressionParser expressionParser,
                                EventPublisher eventPublisher,
                                SoaEventRocketMqProducer soaEventRocketMqProducer) {
        this.eventBusList = eventBusList;
        this.expressionParser = expressionParser;
        this.eventPublisher = eventPublisher;
        this.soaEventRocketMqProducer = soaEventRocketMqProducer;
    }


    @Override
    public void publish(SoaEvent event) {
        soaEventRocketMqProducer.sendMessage(event);
    }

    /**
     * 消费SoaEvent
     *
     * @param event event
     */
    @Override
    public void subscribe(SoaEvent event) {

        if (CollectionUtils.isEmpty(eventBusList)) {
            return;
        }

        boolean anySubscribe = eventBusList
                .stream()
                .anyMatch(eventBus -> {

                    Iterator<Subscriber> subscribers = eventBus.getSubscribers(event);

                    if (subscribers.hasNext()) {
                        while (subscribers.hasNext()) {
                            Subscriber subscriber = subscribers.next();
                            if (subscriber.shouldSubscribe(expressionParser, event)) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
        if (!anySubscribe) {
            return;
        }
        eventPublisher.asyncPublish(event);
    }
}
