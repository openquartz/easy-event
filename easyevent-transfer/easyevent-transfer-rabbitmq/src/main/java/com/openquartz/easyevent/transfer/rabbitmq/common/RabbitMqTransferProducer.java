package com.openquartz.easyevent.transfer.rabbitmq.common;

import com.openquartz.easyevent.common.model.LifecycleBean;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.transfer.api.adapter.SendResultCallback;
import com.openquartz.easyevent.transfer.api.adapter.TransferProducer;
import com.openquartz.easyevent.transfer.api.common.BatchSendResult;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.transfer.api.message.EventMessageBuilder;
import com.openquartz.easyevent.transfer.api.route.EventRouter;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqCommonProperty;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

/**
 * RabbitMQ Transfer Producer
 *
 * @author svnee
 */
@Slf4j
public class RabbitMqTransferProducer implements TransferProducer, LifecycleBean {

    private Connection connection;
    private Channel channel;
    private final Serializer serializer;
    private final EventRouter eventRouter;
    private final RabbitMqCommonProperty rabbitMqCommonProperty;

    public RabbitMqTransferProducer(Serializer serializer,
        EventRouter eventRouter,
        RabbitMqCommonProperty rabbitMqCommonProperty) {

        checkNotNull(serializer);
        checkNotNull(eventRouter);
        checkNotNull(rabbitMqCommonProperty);

        this.serializer = serializer;
        this.eventRouter = eventRouter;
        this.rabbitMqCommonProperty = rabbitMqCommonProperty;
    }

    @Override
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitMqCommonProperty.getHost());
            factory.setPort(rabbitMqCommonProperty.getPort());
            factory.setUsername(rabbitMqCommonProperty.getUsername());
            factory.setPassword(rabbitMqCommonProperty.getPassword());
            factory.setVirtualHost(rabbitMqCommonProperty.getVirtualHost());
            
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
        } catch (Exception ex) {
            log.error("[RabbitMqProducer#init] RabbitMqProducer producer init error", ex);
            ExceptionUtils.rethrow(ex);
        }
    }

    @Override
    public void destroy() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception ex) {
            log.error("[RabbitMqProducer#destroy] RabbitMqProducer producer destroy error", ex);
        }
    }

    @Override
    public <T> void sendMessage(EventBody<T> eventBody, EventId eventId) {
        Pair<String, String> routeInfo = eventRouter.route(eventBody.getEvent());
        String exchangeName = routeInfo.getKey();
        String routingKey = parseRoutingKey(routeInfo);
        
        try {
            // 处理默认exchange的情况（空字符串或"/"）
            boolean isDefaultExchange = exchangeName == null || exchangeName.isEmpty() || "/".equals(exchangeName);
            
            if (!isDefaultExchange) {
                // 声明topic交换机
                channel.exchangeDeclare(exchangeName, "topic", true);
            }
            
            // 构建消息
            EventMessage eventMessage = EventMessageBuilder.builder()
                .eventId(eventId)
                .event(eventBody)
                .serializer(serializer)
                .build();
            
            String messageBody = JSONUtil.toJson(eventMessage);
            
            // 发送消息
            // 如果使用默认exchange，exchangeName应该为空字符串，routingKey应该是队列名
            String finalExchangeName = isDefaultExchange ? "" : exchangeName;
            String finalRoutingKey = isDefaultExchange ? routingKey : routingKey;
            
            channel.basicPublish(finalExchangeName, finalRoutingKey, 
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                    messageBody.getBytes(StandardCharsets.UTF_8));
                
            log.info("[RabbitMQ#sendMessage] send message, exchange:{}, routingKey:{}, queue:{}", 
                finalExchangeName.isEmpty() ? "(default)" : finalExchangeName, finalRoutingKey, 
                isDefaultExchange ? finalRoutingKey : "N/A");
        } catch (Exception ex) {
            log.error("[RabbitMQ#sendMessage] exe-error!,data:{},exchange:{},routingKey:{}", 
                eventBody, exchangeName, routingKey, ex);
            ExceptionUtils.rethrow(ex);
        }
    }

    @Override
    public <T> void asyncSendMessage(EventBody<T> eventBody, EventId eventId, SendResultCallback callback) {
        Pair<String, String> routeInfo = eventRouter.route(eventBody.getEvent());
        String exchangeName = routeInfo.getKey();
        String routingKey = parseRoutingKey(routeInfo);
        
        try {
            // 处理默认exchange的情况（空字符串或"/"）
            boolean isDefaultExchange = exchangeName == null || exchangeName.isEmpty() || "/".equals(exchangeName);
            
            if (!isDefaultExchange) {
                // 声明topic交换机
                channel.exchangeDeclare(exchangeName, "topic", true);
            }
            
            // 构建消息
            EventMessage eventMessage = EventMessageBuilder.builder()
                .eventId(eventId)
                .event(eventBody)
                .serializer(serializer)
                .build();
            
            String messageBody = JSONUtil.toJson(eventMessage);
            
            // 发送消息
            // 如果使用默认exchange，exchangeName应该为空字符串，routingKey应该是队列名
            String finalExchangeName = isDefaultExchange ? "" : exchangeName;
            String finalRoutingKey = isDefaultExchange ? routingKey : routingKey;
            
            channel.basicPublish(finalExchangeName, finalRoutingKey, 
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                    messageBody.getBytes(StandardCharsets.UTF_8));
                
            log.info("[RabbitMQ#asyncSendMessage] send message, exchange:{}, routingKey:{}, queue:{}", 
                finalExchangeName.isEmpty() ? "(default)" : finalExchangeName, finalRoutingKey,
                isDefaultExchange ? finalRoutingKey : "N/A");
                
            if (callback != null) {
                callback.onSuccess(eventId);
            }
        } catch (Exception ex) {
            log.error("[RabbitMQ#asyncSendMessage] exe-error!,data:{},exchange:{},routingKey:{}", 
                eventBody, exchangeName, routingKey, ex);
            if (callback != null) {
                callback.onFail(eventId, ex);
            }
            ExceptionUtils.rethrow(ex);
        }
    }

    private String parseRoutingKey(Pair<String, String> routeInfo) {
        if (Objects.isNull(routeInfo.getValue())) {
            // 如果没有指定routing key，则使用默认的
            return "easyevent.default";
        }
        return routeInfo.getValue();
    }

    @Override
    public <T> BatchSendResult sendMessageList(List<EventBody<T>> eventList, List<EventId> eventIdList) {
        if (CollectionUtils.isEmpty(eventList)) {
            return new BatchSendResult();
        }
        
        BatchSendResult batchSendResult = new BatchSendResult();
        
        List<Pair<Integer, EventBody<?>>> index2EventList = new ArrayList<>(eventList.size());
        for (int i = 0; i < eventList.size(); i++) {
            index2EventList.add(Pair.of(i, eventList.get(i)));
        }
        
        // mapping routeInfo 2 index
        Map<Pair<String, String>, List<Pair<Integer, EventBody<?>>>> routeInfo2EventMap = index2EventList.stream()
            .map(e -> Pair.of(eventRouter.route(e.getValue().getEvent()), e))
            .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));

        for (Map.Entry<Pair<String, String>, List<Pair<Integer, EventBody<?>>>> routeInfo2Event : routeInfo2EventMap.entrySet()) {
            String exchangeName = routeInfo2Event.getKey().getKey();
            String routingKey = parseRoutingKey(routeInfo2Event.getKey());
            
            try {
                // 处理默认exchange的情况（空字符串或"/"）
                boolean isDefaultExchange = exchangeName == null || exchangeName.isEmpty() || "/".equals(exchangeName);
                
                if (!isDefaultExchange) {
                    // 声明topic交换机
                    channel.exchangeDeclare(exchangeName, "topic", true);
                }
                
                String finalExchangeName = isDefaultExchange ? "" : exchangeName;
                
                List<Pair<Integer, EventBody<?>>> eventPairs = routeInfo2Event.getValue();
                List<Integer> completedIndices = new ArrayList<>();
                List<Integer> failedIndices = new ArrayList<>();
                
                for (Pair<Integer, EventBody<?>> eventPair : eventPairs) {
                    Integer index = eventPair.getKey();
                    EventBody<?> eventBody = eventPair.getValue();
                    
                    try {
                        // 构建消息
                        EventMessage eventMessage = EventMessageBuilder.builder()
                            .eventId(eventIdList.get(index))
                            .event(eventBody)
                            .serializer(serializer)
                            .build();
                        
                        String messageBody = JSONUtil.toJson(eventMessage);
                        
                        // 发送消息
                        channel.basicPublish(finalExchangeName, routingKey, 
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                                messageBody.getBytes(StandardCharsets.UTF_8));
                        
                        completedIndices.add(index);
                    } catch (Exception ex) {
                        log.error("[RabbitMQ#sendMessageList] send error!eventId:{},event:{}", 
                            eventIdList.get(index), eventBody, ex);
                        failedIndices.add(index);
                        batchSendResult.setFailedException(ex);
                    }
                }
                
                batchSendResult.addCompletedIndex(completedIndices);
                batchSendResult.addFailedIndex(failedIndices);
                
            } catch (Exception ex) {
                log.error("[RabbitMQ#sendMessageList] exe-error!,exchange:{},routingKey:{}", 
                    exchangeName, routingKey, ex);
                
                // 所有消息都失败
                List<Integer> allIndices = routeInfo2Event.getValue().stream()
                    .map(Pair::getKey)
                    .collect(Collectors.toList());
                batchSendResult.addFailedIndex(allIndices);
                batchSendResult.setFailedException(ex);
            }
        }
        
        return batchSendResult;
    }
}