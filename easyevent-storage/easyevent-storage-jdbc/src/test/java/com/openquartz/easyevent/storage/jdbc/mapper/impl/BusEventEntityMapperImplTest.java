package com.openquartz.easyevent.storage.jdbc.mapper.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.jdbc.table.EasyEventTableGeneratorSupplier;
import com.openquartz.easyevent.storage.model.BusEventEntity;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
    public void testRefreshStartProcessing_casSuccess() {
        long eventIdVal = 789L;
        EventId eventId = new EventId(eventIdVal, 1L);
        EventLifecycleState state = EventLifecycleState.IN_PROCESSING;
        String tableName = "ee_bus_event_entity";

        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn(tableName);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        assertTrue(mapper.refreshStartProcessing(eventId, state));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(
            sqlCaptor.capture(),
            eq(state.getCode()),
            eq(""),
            anyString(),
            eq(eventIdVal)
        );
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("processing_state in ('TRANSFER_SUCCESS','PROCESS_FAILED','TRANSFER_FAILED')"));
        assertTrue(sql.contains("start_execution_time=NOW()"));
    }

    @Test
    public void testRefreshStartProcessing_casMiss() {
        long eventIdVal = 790L;
        EventId eventId = new EventId(eventIdVal, 1L);
        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn("ee_bus_event_entity");
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(0);

        assertFalse(mapper.refreshStartProcessing(eventId, EventLifecycleState.IN_PROCESSING));
    }

    @Test
    public void testIsPresent_whenAbsent_shouldReturnFalse() {
        long eventIdVal = 11L;
        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn("ee_bus_event_entity");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(eventIdVal)))
            .thenReturn(Collections.emptyList());

        assertFalse(mapper.isPresent(eventIdVal));
    }

    @Test
    public void testIsPresent_whenExists_shouldReturnTrue() {
        long eventIdVal = 12L;
        when(supplier.genBusEventEntityTable(eventIdVal)).thenReturn("ee_bus_event_entity");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(eventIdVal)))
            .thenReturn(Collections.singletonList(eventIdVal));

        assertTrue(mapper.isPresent(eventIdVal));
    }

    @Test
    public void testGetBySelectiveCondition_emptyShards_shouldReturnEmpty() {
        when(supplier.genAllBusEventEntityTableList()).thenReturn(Collections.emptyList());
        List<BusEventEntity> result = mapper.getBySelectiveCondition(BusEventSelectorCondition.builder("app", 2));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetBySelectiveCondition_sourceShouldOrderAndTruncate() throws Exception {
        java.nio.file.Path path = java.nio.file.Paths.get(
            "src/main/java/com/openquartz/easyevent/storage/jdbc/mapper/impl/BusEventEntityMapperImpl.java");
        String source = new String(java.nio.file.Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(source.contains("order by id asc limit :offset"));
        assertTrue(source.contains("resultList.subList(0, condition.getOffset())"));
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
