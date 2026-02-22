package com.openquartz.easyevent.storage.jdbc.mapper.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.jdbc.table.EasyEventTableGeneratorSupplier;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class BusEventEntityMapperImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EasyEventTableGeneratorSupplier supplier;

    private BusEventEntityMapperImpl mapper;

    @Before
    public void setUp() {
        mapper = new BusEventEntityMapperImpl(jdbcTemplate, supplier);
    }

    @Test
    public void testRefreshSendComplete() {
        // Arrange
        long eventIdVal = 123L;
        long sourceIdVal = 1L;
        EventId eventId = new EventId(eventIdVal, sourceIdVal);
        EventLifecycleState state = EventLifecycleState.TRANSFER_SUCCESS;
        String tableName = "ee_bus_event_entity";

        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn(tableName);
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenReturn(1); // For update state

        // Act
        mapper.refreshSendComplete(eventId, state);

        // Assert
        // Verify state update
        verify(jdbcTemplate).update(anyString(), eq(state.getCode()), anyString(), eq(eventIdVal));
    }

    @Test
    public void testRefreshSendFailed() {
        // Arrange
        long eventIdVal = 456L;
        long sourceIdVal = 1L;
        EventId eventId = new EventId(eventIdVal, sourceIdVal);
        EventLifecycleState state = EventLifecycleState.TRANSFER_FAILED;
        Throwable ex = new RuntimeException("Connection failed");
        String tableName = "ee_bus_event_entity";

        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn(tableName);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1); // For update state

        // Act
        mapper.refreshSendFailed(eventId, state, ex);

        // Assert
        verify(jdbcTemplate).update(
            anyString(),
            eq(state.getCode()), 
            anyString(), 
            anyString(), 
            eq(eventIdVal)
        );
    }

    @Test
    public void testRefreshStartProcessing() {
        // Arrange
        long eventIdVal = 789L;
        long sourceIdVal = 1L;
        EventId eventId = new EventId(eventIdVal, sourceIdVal);
        EventLifecycleState state = EventLifecycleState.IN_PROCESSING;
        String tableName = "ee_bus_event_entity";

        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn(tableName);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1); // For update state

        // Act
        mapper.refreshStartProcessing(eventId, state);

        // Assert
        verify(jdbcTemplate).update(
            contains("start_execution_time=CASE WHEN processing_state = 'IN_PROCESSING' THEN start_execution_time ELSE NOW() END"),
            eq(state.getCode()),
            eq(""),
            anyString(),
            eq(eventIdVal)
        );
    }

    @Test
    public void testProcessingComplete_ShouldUpdateSuccessTime() {
        // Arrange
        long eventIdVal = 101112L;
        EventId eventId = new EventId(eventIdVal, 1L);
        EventLifecycleState state = EventLifecycleState.PROCESS_COMPLETE;
        String tableName = "ee_bus_event_entity";

        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn(tableName);
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenReturn(1); // For update state

        // Act
        mapper.processingComplete(eventId, state);

        // Assert
        // Verify update call with execution_success_time=NOW()
        verify(jdbcTemplate).update(
            contains("execution_success_time=CASE WHEN processing_state = 'PROCESS_COMPLETE' THEN execution_success_time ELSE NOW() END"), 
            eq(state.getCode()), 
            eq(""), 
            eq(eventIdVal)
        );
    }
}
