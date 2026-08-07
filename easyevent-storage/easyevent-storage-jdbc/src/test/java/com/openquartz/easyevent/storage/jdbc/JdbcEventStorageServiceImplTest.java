package com.openquartz.easyevent.storage.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.jdbc.mapper.BusEventEntityMapper;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcEventStorageServiceImplTest {

    @Mock
    private BusEventEntityMapper mapper;
    @Mock
    private Serializer serializer;
    @Mock
    private EasyEventProperties properties;

    private JdbcEventStorageServiceImpl service;

    @Before
    public void setUp() {
        service = new JdbcEventStorageServiceImpl(mapper, serializer, null, properties);
    }

    @Test
    public void startProcessing_shouldDelegateCasResult() {
        EventId eventId = new EventId(1L, 1L);
        when(mapper.refreshStartProcessing(eventId, EventLifecycleState.IN_PROCESSING)).thenReturn(false);

        assertFalse(service.startProcessing(eventId));
        verify(mapper).refreshStartProcessing(eq(eventId), eq(EventLifecycleState.IN_PROCESSING));
    }

    @Test
    public void isMoreThanMustTrigger_shouldUseInclusiveBoundary() {
        when(properties.getMaxRetryCount()).thenReturn(10);
        BaseEventEntity entity = new BaseEventEntity();
        entity.setErrorCount(10);

        assertTrue(service.isMoreThanMustTrigger(entity));

        entity.setErrorCount(9);
        assertFalse(service.isMoreThanMustTrigger(entity));
    }
}
