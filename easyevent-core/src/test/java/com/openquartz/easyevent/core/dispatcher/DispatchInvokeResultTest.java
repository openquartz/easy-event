package com.openquartz.easyevent.core.dispatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.core.Subscriber;
import org.junit.Test;

public class DispatchInvokeResultTest {

    @Test
    public void toString_whenInvokeErrorNull_shouldNotThrowNpe() {
        DispatchInvokeResult result = new DispatchInvokeResult("event");
        String text = result.toString();
        assertTrue(text.contains("invokeResult=true"));
        assertTrue(text.contains("invokeError=null"));
    }

    @Test
    public void merge_sameEvent_shouldMergeSuccessAndError() {
        Object event = new Object();
        DispatchInvokeResult left = new DispatchInvokeResult(event);
        DispatchInvokeResult right = new DispatchInvokeResult(event);
        Subscriber subscriber = mock(Subscriber.class);
        when(subscriber.getTargetIdentify()).thenReturn("sub-1");
        left.addSubscriber(subscriber);
        right.setInvokeError(new IllegalStateException("boom"));

        DispatchInvokeResult merged = left.merge(right);

        assertFalse(merged.isSuccess());
        assertEquals(1, merged.getSuccessSubscriberList().size());
        assertEquals("boom", merged.getInvokeError().getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void merge_differentEvent_shouldThrow() {
        DispatchInvokeResult left = new DispatchInvokeResult("a");
        DispatchInvokeResult right = new DispatchInvokeResult("b");
        left.merge(right);
    }
}
