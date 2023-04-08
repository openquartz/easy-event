package com.openquartz.easyevent.example.limiting;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.openquartz.easyevent.example.exception.LimingErrorCode;
import java.util.Collections;
import java.util.function.Consumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.LimitingBlockedException;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * SentinelEventTransferTriggerLimitingControl
 *
 * @author svnee
 **/
@Component
public class SentinelEventTransferTriggerLimitingControl implements EventTransferTriggerLimitingControl,
    InitializingBean {

    private static final String KEY = "event.transfer.trigger";

    @Override
    public void control(EventMessage eventMessage, Consumer<EventMessage> eventHandleFunction) {
        Entry entry = null;
        try {
            ContextUtil.enter(KEY);
            entry = SphU.entry(KEY, EntryType.OUT);
            //
            eventHandleFunction.accept(eventMessage);
        } catch (BlockException ex) {
            // Blocked.
            throw new LimitingBlockedException(LimingErrorCode.CONSUME_LIMITING_BLOCKED_ERROR);
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    @Override
    public void afterPropertiesSet() {
        FlowRule rule = new FlowRule();
        rule.setResource(KEY);
        // Indicates the interval between two adjacent requests is 200 ms.
        rule.setCount(5);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");

        // Enable rate limiting (uniform). This can ensure fixed intervals between two adjacent calls.
        // In this example, intervals between two incoming calls (message consumption) will be 200 ms constantly.
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        // If more requests are coming, they'll be put into the waiting queue.
        // The queue has a queueing timeout. Requests that may exceed the timeout will be immediately blocked.
        // In this example, the max timeout is 5s.
        rule.setMaxQueueingTimeMs(5 * 1000);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}
