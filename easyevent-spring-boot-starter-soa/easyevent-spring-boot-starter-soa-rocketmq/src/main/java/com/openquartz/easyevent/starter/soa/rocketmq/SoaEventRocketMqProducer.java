package com.openquartz.easyevent.starter.soa.rocketmq;

import com.openquartz.easyevent.common.exception.CommonErrorCode;
import com.openquartz.easyevent.common.exception.EasyEventException;
import com.openquartz.easyevent.common.model.LifecycleBean;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.starter.soa.api.SoaEvent;
import com.openquartz.easyevent.storage.model.EventBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

import java.util.Objects;

/**
 * RocketMQ 发送者
 *
 * @author svnee
 **/
@Slf4j
public class SoaEventRocketMqProducer implements LifecycleBean {

    private final MQProducer producer;
    private final SoaEventRocketMqCommonProperty soaEventRocketMqCommonProperty;

    public SoaEventRocketMqProducer(MQProducer producer,
                                    SoaEventRocketMqCommonProperty soaEventRocketMqCommonProperty) {
        this.producer = producer;
        this.soaEventRocketMqCommonProperty = soaEventRocketMqCommonProperty;
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

    public <T extends SoaEvent> void sendMessage(EventBody<T> eventBody) {

        Message message = new Message();
        message.setBody(JSONUtil.toJsonAsBytes(eventBody));
        message.setTopic(soaEventRocketMqCommonProperty.getTopic());
        message.setTags(eventBody.getEvent().getClass().getName());
        if (eventBody.getEvent().getEventKey() != null) {
            message.setKeys(eventBody.getEvent().getEventKey());
        }
        SendResult result = null;
        try {
            log.info("[RocketMQ#sendMessage],data:{},topic:{}", eventBody, soaEventRocketMqCommonProperty.getTopic());
            result = producer.send(message, soaEventRocketMqCommonProperty.getProduceTimeout());
            log.info("[RocketMQ#sendMessage],sendResult:{},data:{},topic:{}",
                    result, eventBody, soaEventRocketMqCommonProperty.getTopic());
        } catch (InterruptedException ex) {
            log.error("[RocketMQ#sendMessage]exe-interrupt!,data:{},topic:{}",
                    eventBody, soaEventRocketMqCommonProperty.getTopic(), ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        } catch (Exception ex) {
            log.error("[RocketMQ#sendMessage]exe-error!,data:{},topic:{}",
                    eventBody, soaEventRocketMqCommonProperty.getTopic(), ex);
            ExceptionUtils.rethrow(ex);
        }
        if (Objects.isNull(result) || !SendStatus.SEND_OK.equals(result.getSendStatus())) {
            throw EasyEventException.replacePlaceHold(CommonErrorCode.THREAD_EXECUTE_EXCEPTION_NULLABLE_ERROR);
        }
    }

}
