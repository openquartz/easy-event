package com.openquartz.easyevent.transfer.rocket.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.transfer.api.adapter.SendResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import com.openquartz.easyevent.common.exception.EasyEventException;
import com.openquartz.easyevent.common.model.LifecycleBean;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.transfer.api.adapter.TransferProducer;
import com.openquartz.easyevent.transfer.api.common.BatchSendResult;
import com.openquartz.easyevent.transfer.api.constant.TransferConstants;
import com.openquartz.easyevent.transfer.api.exception.TransferErrorCode;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.transfer.api.message.EventMessageBuilder;
import com.openquartz.easyevent.transfer.api.route.EventRouter;
import com.openquartz.easyevent.transfer.rocket.property.RocketMqCommonProperty;

/**
 * RocketMQ 发送者
 *
 * @author svnee
 **/
@Slf4j
public class RocketMqProducer implements TransferProducer, LifecycleBean {

    private final MQProducer producer;
    private final Serializer serializer;
    private final EventRouter eventRouter;
    private final RocketMqCommonProperty rocketMqCommonProperty;

    public RocketMqProducer(MQProducer producer,
                            Serializer serializer,
                            EventRouter eventRouter,
                            RocketMqCommonProperty rocketMqCommonProperty) {
        this.producer = producer;
        this.serializer = serializer;
        this.eventRouter = eventRouter;
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

    @Override
    public <T> void sendMessage(EventBody<T> eventBody, EventId eventId) {

        Pair<String, String> routeTopic = eventRouter.route(eventBody.getEvent());
        Message message = buildSendMessage(eventBody, eventId, routeTopic);

        SendResult result = null;
        try {
            log.info("[RocketMQ#sendMessage],data:{},tag:{},topic:{}", eventBody, routeTopic.getRight(),
                    routeTopic.getLeft());
            result = producer.send(message, rocketMqCommonProperty.getProduceTimeout());
            log.info("[RocketMQ#sendMessage],sendResult:{},data:{},tag:{},topic:{}", result, eventBody,
                    routeTopic.getRight(), routeTopic.getLeft());
        } catch (InterruptedException ex) {
            log.error("[RocketMQ#sendMessage]exe-interrupt!,data:{},tag:{},topic:{}",
                    eventBody, routeTopic.getRight(), routeTopic.getLeft(), ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        } catch (Exception ex) {
            log.error("[RocketMQ#sendMessage]exe-error!,data:{},tag:{},topic:{}",
                    eventBody, routeTopic.getRight(), routeTopic.getLeft(), ex);
            ExceptionUtils.rethrow(ex);
        }
        if (Objects.isNull(result) || !SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw EasyEventException.replacePlaceHold(TransferErrorCode.SENDER_FAILED,
                    Objects.nonNull(result) ? result.getMsgId() : StringUtils.EMPTY, routeTopic.getLeft(),
                    routeTopic.getRight());
        }
    }

    @Override
    public <T> void asyncSendMessage(EventBody<T> eventBody, EventId eventId, SendResultCallback callback) {

        Pair<String, String> topic = eventRouter.route(eventBody.getEvent());
        Message message = buildSendMessage(eventBody, eventId, topic);

        try {
            log.info("[RocketMQ#asyncSendMessage],data:{},tag:{},topic:{}", eventBody, topic.getRight(), topic.getLeft());
            producer.send(message, new SendCallback() {

                @Override
                public void onSuccess(SendResult result) {

                    if (Objects.nonNull(result) && SendStatus.SEND_OK.equals(result.getSendStatus())) {
                        callback.onSuccess(eventId);
                        return;
                    }

                    EasyEventException sendError = EasyEventException.replacePlaceHold(TransferErrorCode.SENDER_FAILED,
                            Objects.nonNull(result) ? result.getMsgId() : StringUtils.EMPTY, topic.getLeft(),
                            topic.getRight());
                    callback.onFail(eventId, sendError);
                }

                @Override
                public void onException(Throwable e) {
                    callback.onFail(eventId, e);
                }
            }, rocketMqCommonProperty.getProduceTimeout());

            log.info("[RocketMQ#asyncSendMessage] data:{},tag:{},topic:{}", eventBody, topic.getRight(), topic.getLeft());
        } catch (InterruptedException ex) {
            log.error("[RocketMQ#asyncSendMessage]exe-interrupt!,data:{},tag:{},topic:{}",
                    eventBody, topic.getRight(), topic.getLeft(), ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        } catch (Exception ex) {
            log.error("[RocketMQ#asyncSendMessage]exe-error!,data:{},tag:{},topic:{}",
                    eventBody, topic.getRight(), topic.getLeft(), ex);
            ExceptionUtils.rethrow(ex);
        }
    }

    private <T> Message buildSendMessage(EventBody<T> eventBody, EventId eventId, Pair<String, String> routeTopic) {

        EventMessage eventMessage = EventMessageBuilder.builder()
                .eventId(eventId)
                .event(eventBody)
                .serializer(serializer)
                .build();

        Message message = new Message();
        message.setBody(JSONUtil.toJsonAsBytes(eventMessage));
        message.setTags(Objects.nonNull(routeTopic.getRight()) ? routeTopic.getRight() : TransferConstants.DEFAULT_TAG);
        message.setTopic(routeTopic.getLeft());
        message.setKeys(eventBody.computeEventKey());
        return message;
    }

    @Override
    public <T> BatchSendResult sendMessageList(List<EventBody<T>> eventBodyList, List<EventId> eventIdList) {
        if (CollectionUtils.isEmpty(eventBodyList)) {
            return new BatchSendResult();
        }
        List<Pair<Integer, EventBody<?>>> index2EventList = new ArrayList<>(eventBodyList.size());
        for (int i = 0; i < eventBodyList.size(); i++) {
            index2EventList.add(Pair.of(i, eventBodyList.get(i)));
        }
        // mapping routeInfo 2 index
        Map<Pair<String, String>, List<Pair<Integer, EventBody<?>>>> routeInfo2EventMap = index2EventList.stream()
                .map(e -> Pair.of(eventRouter.route(e.getValue().getEvent()), e))
                .collect(Collectors.groupingBy(Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toList())));

        BatchSendResult batchSendResult = new BatchSendResult();

        for (Entry<Pair<String, String>, List<Pair<Integer, EventBody<?>>>> routeInfo2Event : routeInfo2EventMap.entrySet()) {
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
                        message.setTags(
                                Objects.nonNull(routeInfo2Event.getKey().getValue()) ? routeInfo2Event.getKey().getValue()
                                        : TransferConstants.DEFAULT_TAG);
                        message.setKeys(e.getValue().computeEventKey());
                        return message;
                    }).collect(Collectors.toList());

            // split rocket message
            RocketMqListMessageSplitter splitter = new RocketMqListMessageSplitter(messageList,
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
