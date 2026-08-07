package com.openquartz.easyevent.core.publisher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.dispatcher.DispatchInvokeResult;
import com.openquartz.easyevent.core.intreceptor.PublisherInterceptor;
import com.openquartz.easyevent.core.intreceptor.PublisherInterceptorChain;
import com.openquartz.easyevent.core.intreceptor.PublisherInterceptorContext;
import com.openquartz.easyevent.transfer.api.EventSender;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventPublisherAdapterTest {

    private EventBus eventBus;
    private EventSender eventSender;
    private EventPublisherAdapter publisher;

    @Before
    public void setUp() throws Exception {
        clearInterceptors();
        eventBus = mock(EventBus.class);
        eventSender = mock(EventSender.class);
        publisher = new EventPublisherAdapter() {
            @Override
            public EventBus getDirectEventBus() {
                return eventBus;
            }

            @Override
            public EventSender getEventSender() {
                return eventSender;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        clearInterceptors();
    }

    @SuppressWarnings("unchecked")
    private static void clearInterceptors() throws Exception {
        Field field = PublisherInterceptorChain.class.getDeclaredField("INTERCEPTOR_LIST");
        field.setAccessible(true);
        ((List<PublisherInterceptor>) field.get(null)).clear();
    }

    @Test
    public void syncPublish_whenPreInterceptorVeto_shouldReturnFalse() {
        PublisherInterceptorChain.addInterceptor(new PublisherInterceptor() {
            @Override
            public boolean prePublish(Object event, PublisherInterceptorContext context) {
                return false;
            }
        });

        boolean result = publisher.syncPublish("event");

        assertFalse(result);
        verify(eventBus, never()).post(any());
    }

    @Test
    public void asyncPublish_whenPreInterceptorVeto_shouldReturnFalse() {
        PublisherInterceptorChain.addInterceptor(new PublisherInterceptor() {
            @Override
            public boolean prePublish(Object event, PublisherInterceptorContext context) {
                return false;
            }
        });

        boolean result = publisher.asyncPublish("event");

        assertFalse(result);
        verify(eventSender, never()).send(any());
    }

    @Test
    public void syncPublish_whenAllowed_shouldReturnBusResult() {
        when(eventBus.post(any())).thenReturn(new DispatchInvokeResult("event"));

        boolean result = publisher.syncPublish("event");

        assertTrue(result);
        verify(eventBus).post("event");
    }

    @Test
    public void asyncPublish_whenAllowed_shouldReturnSenderResult() {
        when(eventSender.send(any())).thenReturn(true);

        boolean result = publisher.asyncPublish("event");

        assertTrue(result);
        verify(eventSender).send("event");
    }
}
