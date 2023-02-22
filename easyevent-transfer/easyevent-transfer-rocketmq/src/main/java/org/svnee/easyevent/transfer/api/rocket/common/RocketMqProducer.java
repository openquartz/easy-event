package org.svnee.easyevent.transfer.api.rocket.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.svnee.easyevent.common.exception.EasyEventException;
import org.svnee.easyevent.common.model.LifecycleBean;
import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.common.utils.ExceptionUtils;
import org.svnee.easyevent.common.utils.JSONUtil;
import org.svnee.easyevent.common.utils.StringUtils;
import org.svnee.easyevent.storage.identify.EventId;
import org.svnee.easyevent.transfer.api.exception.TransferErrorCode;
import org.svnee.easyevent.transfer.api.message.EventMessage;
import org.svnee.easyevent.transfer.api.message.EventMessageBuilder;
import org.svnee.easyevent.transfer.api.rocket.property.RocketMqCommonProperty;
import org.svnee.easyevent.transfer.api.route.EventRouteStrategy;

/**
 * RocketMQ 发送者
 *
 * @author svnee
 **/
@Slf4j
public class RocketMqProducer implements LifecycleBean {

    private final MQProducer producer;
    private final Serializer serializer;
    private final EventRouteStrategy eventRouteStrategy;
    private final RocketMqCommonProperty rocketMqCommonProperty;

    public RocketMqProducer(MQProducer producer,
        Serializer serializer,
        EventRouteStrategy eventRouteStrategy,
        RocketMqCommonProperty rocketMqCommonProperty) {
        this.producer = producer;
        this.serializer = serializer;
        this.eventRouteStrategy = eventRouteStrategy;
        this.rocketMqCommonProperty = rocketMqCommonProperty;
    }

    @Override
    public void init() {
        try {
            producer.start();
        } catch (Exception ex) {
            log.error("[RocketMqProducer#init]RocketMqProducer producer start error", ex);
            ExceptionUtils.rethrow(ex);
        }
    }

    @Override
    public void destroy() {
        producer.shutdown();
    }

    public <T> void sendMessage(T event, EventId eventId) {

        EventMessage eventMessage = EventMessageBuilder.builder()
            .eventId(eventId)
            .event(event)
            .serializer(serializer)
            .build();
        Pair<String, String> routeTopic = eventRouteStrategy.route(event);

        Message message = new Message();
        message.setBody(JSONUtil.toJsonAsBytes(eventMessage));
        message.setTags(routeTopic.getRight());
        message.setTopic(routeTopic.getLeft());
        message.setKeys(String.valueOf(eventId.getId()));
        SendResult result = null;
        try {
            log.info("[RocketMQ#sendMessage],data:{},tag:{},topic:{}", event, routeTopic.getRight(),
                routeTopic.getLeft());
            result = producer.send(message, rocketMqCommonProperty.getProduceTimeout());
            log.info("[RocketMQ#sendMessage],sendResult:{},data:{},tag:{},topic:{}", result, event,
                routeTopic.getRight(), routeTopic.getLeft());
        } catch (InterruptedException ex) {
            log.error("[RocketMQ#sendMessage]exe-interrupt!,data:{},tag:{},topic:{}",
                event, routeTopic.getRight(), routeTopic.getLeft(), ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        } catch (Exception ex) {
            log.error("[RocketMQ#sendMessage]exe-error!,data:{},tag:{},topic:{}",
                event, routeTopic.getRight(), routeTopic.getLeft(), ex);
            ExceptionUtils.rethrow(ex);
        }
        if (Objects.isNull(result) || !SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw EasyEventException.replacePlaceHold(TransferErrorCode.SENDER_FAILED,
                Objects.nonNull(result) ? result.getMsgId() : StringUtils.EMPTY, routeTopic.getLeft(),
                routeTopic.getRight());
        }
    }

    public <T> BatchSendResult sendMessageList(List<T> eventList, List<EventId> eventIdList) {
        if (CollectionUtils.isEmpty(eventList)) {
            return new BatchSendResult();
        }
        List<Pair<Integer, Object>> index2EventList = new ArrayList<>(eventList.size());
        for (int i = 0; i < eventList.size(); i++) {
            index2EventList.add(Pair.of(i, eventList.get(i)));
        }
        // mapping routeInfo 2 index
        Map<Pair<String, String>, List<Pair<Integer, Object>>> routeInfo2EventMap = index2EventList.stream()
            .map(e -> Pair.of(eventRouteStrategy.route(e.getValue()), e))
            .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));

        BatchSendResult batchSendResult = new BatchSendResult();

        for (Entry<Pair<String, String>, List<Pair<Integer, Object>>> routeInfo2Event : routeInfo2EventMap.entrySet()) {
            List<Message> messageList = routeInfo2Event.getValue()
                .stream()
                .map(e -> {
                    EventMessage eventMessage = EventMessageBuilder.builder()
                        .eventId(eventIdList.get(e.getKey()))
                        .event(e.getValue())
                        .serializer(serializer)
                        .build();
                    Message message = new Message();
                    message.setBody(JSONUtil.toJsonAsBytes(eventMessage));
                    message.setTopic(routeInfo2Event.getKey().getKey());
                    message.setTags(routeInfo2Event.getKey().getValue());
                    message.setKeys(String.valueOf(eventIdList.get(e.getKey())));
                    return message;
                }).collect(Collectors.toList());

            // split rocket message
            RocketMQListMessageSplitter splitter = new RocketMQListMessageSplitter(messageList,
                rocketMqCommonProperty.getProduceMessageSize());

            int startIndex = 0;
            int beforeLen = 0;
            while (splitter.hasNext()) {
                List<Message> listItem = splitter.next();
                startIndex += beforeLen;
                beforeLen = listItem.size();
                List<Integer> completedEventIndexList = routeInfo2Event.getValue()
                    .subList(startIndex, startIndex + listItem.size())
                    .stream()
                    .map(Pair::getKey)
                    .collect(Collectors.toList());
                try {
                    SendResult sendResult = producer.send(listItem, rocketMqCommonProperty.getProduceTimeout());
                    if (Objects.nonNull(sendResult) && SendStatus.SEND_OK == sendResult.getSendStatus()) {
                        batchSendResult.addCompletedIndex(completedEventIndexList);
                    } else {
                        batchSendResult.addFailedIndex(completedEventIndexList);
                    }
                } catch (InterruptedException ex) {
                    log.error("[RocketMQ#sendMessageList]exe-interrupt!,tag:{},topic:{}",
                        routeInfo2Event.getKey().getRight(), routeInfo2Event.getKey().getLeft(), ex);
                    Thread.currentThread().interrupt();
                    ExceptionUtils.rethrow(ex);
                } catch (Exception ex) {
                    batchSendResult.addFailedIndex(completedEventIndexList);
                    batchSendResult.setFailedException(ex);
                    log.error("[RocketMQ#sendMessageList]exe-error!,tag:{},topic:{}",
                        routeInfo2Event.getKey().getRight(), routeInfo2Event.getKey().getLeft(), ex);
                }
            }
        }
        return batchSendResult;
    }
}
