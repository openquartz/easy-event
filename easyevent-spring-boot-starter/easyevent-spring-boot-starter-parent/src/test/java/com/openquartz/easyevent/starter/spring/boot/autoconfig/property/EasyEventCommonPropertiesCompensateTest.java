package com.openquartz.easyevent.starter.spring.boot.autoconfig.property;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProcessProperty;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProperty;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import java.util.List;
import org.junit.jupiter.api.Test;

public class EasyEventCommonPropertiesCompensateTest {

    @Test
    public void defaultCompensateState_shouldIncludeTransferSuccess() {
        EventCompensateProcessProperty property = new EventCompensateProcessProperty();
        List<EventLifecycleState> states = property.getCompensateState();
        assertTrue(states.contains(EventLifecycleState.TRANSFER_SUCCESS));
        assertTrue(states.contains(EventLifecycleState.AVAILABLE));
        assertTrue(states.contains(EventLifecycleState.TRANSFER_FAILED));
        assertTrue(states.contains(EventLifecycleState.PROCESS_FAILED));
    }

    @Test
    public void blankCompensateState_shouldReturnEmptyList() {
        EventCompensateProcessProperty property = new EventCompensateProcessProperty();
        property.setCompensateState("");
        assertTrue(property.getCompensateState().isEmpty());
    }

    @Test
    public void defaultCompensateState_shouldExcludeInProcessingAndProcessComplete() {
        EventCompensateProcessProperty property = new EventCompensateProcessProperty();
        List<EventLifecycleState> states = property.getCompensateState();
        assertFalse(states.contains(EventLifecycleState.IN_PROCESSING));
        assertFalse(states.contains(EventLifecycleState.PROCESS_COMPLETE));
    }

    @Test
    public void nestedSelfAndGlobalDefaults_shouldBothIncludeTransferSuccess() {
        EventCompensateProperty compensate = new EasyEventCommonProperties().getCompensate();
        assertTrue(compensate.getSelf().getCompensateState().contains(EventLifecycleState.TRANSFER_SUCCESS));
        assertTrue(compensate.getGlobal().getCompensateState().contains(EventLifecycleState.TRANSFER_SUCCESS));
    }

    @Test
    public void trimAndParseCompensateState_shouldWork() {
        EventCompensateProcessProperty property = new EventCompensateProcessProperty();
        property.setCompensateState(" AVAILABLE , TRANSFER_SUCCESS ");
        List<EventLifecycleState> states = property.getCompensateState();
        assertTrue(states.contains(EventLifecycleState.AVAILABLE));
        assertTrue(states.contains(EventLifecycleState.TRANSFER_SUCCESS));
    }
}
