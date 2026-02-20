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
    public void testRefreshSendComplete_ShouldSaveHistory() {
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
        
        // Verify history insert
        // INSERT_HISTORY_SQL = "insert into ee_bus_event_history(entity_id, status, context, create_time) values(?, ?, ?, NOW())"
        verify(jdbcTemplate).update(
            eq("insert into ee_bus_event_history(entity_id, status, context, create_time) values(?, ?, ?, NOW())"),
            eq(eventIdVal),
            eq(state.getCode()),
            eq("Send Complete")
        );
    }

    @Test
    public void testRefreshSendFailed_ShouldSaveHistory() {
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
        // Verify history insert
        verify(jdbcTemplate).update(
            eq("insert into ee_bus_event_history(entity_id, status, context, create_time) values(?, ?, ?, NOW())"),
            eq(eventIdVal),
            eq(state.getCode()),
            eq("Send Failed: Connection failed")
        );
    }

    @Test
    public void testRefreshStartProcessing_ShouldSaveHistory() {
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
        // Verify history insert
        verify(jdbcTemplate).update(
            eq("insert into ee_bus_event_history(entity_id, status, context, create_time) values(?, ?, ?, NOW())"),
            eq(eventIdVal),
            eq(state.getCode()),
            eq("Start Processing")
        );
    }

    @Test
    public void testProcessingComplete_ShouldUpdateSuccessTimeAndSaveHistory() {
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
        
        // Verify history
        verify(jdbcTemplate).update(
            contains("insert into ee_bus_event_history"),
            eq(eventIdVal),
            eq(state.getCode()),
            contains("Process State Updated")
        );
    }
}
