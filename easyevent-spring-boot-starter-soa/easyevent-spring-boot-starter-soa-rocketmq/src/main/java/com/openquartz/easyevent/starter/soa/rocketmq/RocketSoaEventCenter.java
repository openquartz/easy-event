package com.openquartz.easyevent.starter.soa.rocketmq;

import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.Subscriber;
import com.openquartz.easyevent.core.expression.ExpressionParser;
import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.starter.soa.api.SoaEvent;
import com.openquartz.easyevent.starter.soa.core.SoaEventCenter;
import com.openquartz.easyevent.starter.soa.core.SoaEventHandler;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.storage.model.EventContext;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * RocketMqEventCenter
 *
 * @author svnee
 */
public class RocketSoaEventCenter implements SoaEventCenter {

    private final EasyEventProperties easyEventProperties;
    private final List<EventBus> eventBusList;
    private final ExpressionParser expressionParser;
    private final EventPublisher eventPublisher;
    private final SoaEventRocketMqProducer soaEventRocketMqProducer;

    public RocketSoaEventCenter(EasyEventProperties easyEventProperties,
                                List<EventBus> eventBusList,
                                ExpressionParser expressionParser,
                                EventPublisher eventPublisher,
                                SoaEventRocketMqProducer soaEventRocketMqProducer) {
        this.easyEventProperties = easyEventProperties;
        this.eventBusList = eventBusList;
        this.expressionParser = expressionParser;
        this.eventPublisher = eventPublisher;
        this.soaEventRocketMqProducer = soaEventRocketMqProducer;
    }


    @Override
    public void publish(SoaEvent event) {
        EventBody<SoaEvent> eventBody = new EventBody<>(event, EventContext.get());
        soaEventRocketMqProducer.sendMessage(eventBody);
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

        if (Objects.equals(event.getSoaIdentify(), easyEventProperties.getAppId())) {
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
