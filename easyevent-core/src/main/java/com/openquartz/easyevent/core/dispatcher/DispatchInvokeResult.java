package com.openquartz.easyevent.core.dispatcher;

import com.openquartz.easyevent.core.Subscriber;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.Data;
import com.openquartz.easyevent.common.utils.CollectionUtils;

/**
 * @author svnee
 **/
@Data
public class DispatchInvokeResult {

    /**
     * event
     */
    private final Object event;

    /**
     * 执行异常
     */
    private Exception invokeError;

    /**
     * 执行成功订阅者
     */
    private List<Subscriber> successSubscriberList;

    public DispatchInvokeResult(Object event) {
        this.event = event;
    }

    public void addSubscriberList(List<Subscriber> successSubscriberList) {
        if (CollectionUtils.isEmpty(successSubscriberList)) {
            return;
        }
        initSuccessSubscriberListIfAbsent();
        this.successSubscriberList.addAll(successSubscriberList);
    }

    public List<Subscriber> getSuccessSubscriberList() {
        return Objects.isNull(successSubscriberList) ? Collections.emptyList() : successSubscriberList;
    }

    private void initSuccessSubscriberListIfAbsent() {
        if (this.successSubscriberList == null) {
            this.successSubscriberList = new ArrayList<>();
        }
    }

    public void addSubscriber(Subscriber subscriber) {
        if (Objects.isNull(subscriber)) {
            return;
        }
        initSuccessSubscriberListIfAbsent();
        this.successSubscriberList.add(subscriber);
    }

    /**
     * 清空已经执行成功的订阅者信息
     */
    public void clear() {
        if (Objects.nonNull(successSubscriberList)) {
            this.successSubscriberList.clear();
        }
    }

    public boolean isSuccess() {
        return Objects.isNull(invokeError);
    }

    public DispatchInvokeResult merge(DispatchInvokeResult result) {
        if (!Objects.equals(result.getEvent(), this.event)) {
            throw new IllegalArgumentException("not support diff event merge!");
        }
        DispatchInvokeResult mergeInvokeResult = new DispatchInvokeResult(this.event);
        Exception mergeInvokeError =
            Objects.nonNull(this.getInvokeError()) ? getInvokeError() : result.getInvokeError();
        mergeInvokeResult.addSubscriberList(this.getSuccessSubscriberList());
        mergeInvokeResult.addSubscriberList(result.getSuccessSubscriberList());
        mergeInvokeResult.setInvokeError(mergeInvokeError);
        return mergeInvokeResult;
    }

    @Override
    public String toString() {

        StringJoiner joiner = new StringJoiner(",");
        if (CollectionUtils.isNotEmpty(successSubscriberList)) {
            for (Subscriber subscriber : successSubscriberList) {
                joiner.add(subscriber.getTargetIdentify());
            }
        }

        return "DispatchInvokeResult{" +
            "event=" + event +
            ", invokeResult=" + isSuccess() +
            ", invokeError=" + invokeError.getMessage() +
            ", successSubscriber=[" + joiner +
            ']';
    }
}
