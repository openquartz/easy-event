package com.openquartz.easyevent.admin.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

public class BusEventDaoRetrySqlTest {

    @Test
    public void mysqlUpdateForRetry_shouldResetStateAndCreatedTime() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), anyLong(), anyLong())).thenReturn(1);

        BusEventDaoMySQL dao = new BusEventDaoMySQL(jdbcTemplate);
        dao.updateForRetry(Arrays.asList(1L, 2L));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), eq(1L), eq(2L));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("processing_state = 'PROCESS_FAILED'"));
        assertTrue(sql.contains("DATE_SUB(NOW(), INTERVAL 2 MINUTE)"));
        assertTrue(sql.contains("error_count = 0"));
    }

    @Test
    public void h2UpdateForRetry_shouldResetStateAndCreatedTime() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(anyString(), anyLong())).thenReturn(1);

        BusEventDaoH2 dao = new BusEventDaoH2(jdbcTemplate);
        dao.updateForRetry(Collections.singletonList(3L));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), eq(3L));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("processing_state = 'PROCESS_FAILED'"));
        assertTrue(sql.contains("DATEADD('MINUTE', -2, CURRENT_TIMESTAMP)"));
    }
}
