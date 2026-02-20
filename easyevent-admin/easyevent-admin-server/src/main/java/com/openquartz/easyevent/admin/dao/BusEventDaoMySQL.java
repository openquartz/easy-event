package com.openquartz.easyevent.admin.dao;

import com.openquartz.easyevent.admin.model.BusEventEntity;
import com.openquartz.easyevent.admin.model.BusEventHistoryEntity;
import com.openquartz.easyevent.admin.model.query.EventQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@Profile("!h2")
@RequiredArgsConstructor
public class BusEventDaoMySQL implements BusEventDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT = "SELECT id, app_id, source_id, class_name, error_count, processing_state, " +
            "successful_subscriber, trace_id, event_data, event_key, creating_owner, processing_owner, " +
            "processing_available_date, processing_failed_reason, created_time, updated_time, start_execution_time, execution_success_time FROM ee_bus_event_entity";

    private static final RowMapper<BusEventEntity> ROW_MAPPER = new RowMapper<BusEventEntity>() {
        @Override
        public BusEventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            BusEventEntity entity = new BusEventEntity();
            entity.setId(rs.getLong("id"));
            entity.setAppId(rs.getString("app_id"));
            entity.setSourceId(rs.getLong("source_id"));
            entity.setClassName(rs.getString("class_name"));
            entity.setErrorCount(rs.getInt("error_count"));
            entity.setProcessingState(rs.getString("processing_state"));
            entity.setSuccessfulSubscriber(rs.getString("successful_subscriber"));
            entity.setTraceId(rs.getString("trace_id"));
            entity.setEventData(rs.getString("event_data"));
            entity.setEventKey(rs.getString("event_key"));
            entity.setCreatingOwner(rs.getString("creating_owner"));
            entity.setProcessingOwner(rs.getString("processing_owner"));
            entity.setProcessingAvailableDate(rs.getTimestamp("processing_available_date"));
            entity.setProcessingFailedReason(rs.getString("processing_failed_reason"));
            entity.setCreatedTime(rs.getTimestamp("created_time"));
            entity.setUpdatedTime(rs.getTimestamp("updated_time"));
            entity.setStartExecutionTime(rs.getTimestamp("start_execution_time"));
            entity.setExecutionSuccessTime(rs.getTimestamp("execution_success_time"));
            return entity;
        }
    };

    private static final RowMapper<BusEventHistoryEntity> HISTORY_ROW_MAPPER = (rs, rowNum) -> {
        BusEventHistoryEntity entity = new BusEventHistoryEntity();
        entity.setId(rs.getLong("id"));
        entity.setEntityId(rs.getLong("entity_id"));
        entity.setStatus(rs.getString("status"));
        entity.setContext(rs.getString("context"));
        entity.setCreateTime(rs.getTimestamp("create_time"));
        return entity;
    };

    @Override
    public long count(EventQuery query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ee_bus_event_entity WHERE 1=1");
        List<Object> params = new ArrayList<>();
        buildWhere(sql, params, query);
        return jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
    }

    @Override
    public List<BusEventEntity> selectPage(EventQuery query) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        buildWhere(sql, params, query);
        
        sql.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");
        params.add(query.getSize());
        params.add((long) (query.getPage() - 1) * query.getSize());
        
        return jdbcTemplate.query(sql.toString(), params.toArray(), ROW_MAPPER);
    }

    private void buildWhere(StringBuilder sql, List<Object> params, EventQuery query) {
        if (query.getSourceId() != null) {
            sql.append(" AND source_id = ?");
            params.add(query.getSourceId());
        }
        if (StringUtils.hasText(query.getEventKey())) {
            sql.append(" AND event_key LIKE ?");
            params.add("%" + query.getEventKey() + "%");
        }
        if (StringUtils.hasText(query.getProcessingState())) {
            sql.append(" AND processing_state = ?");
            params.add(query.getProcessingState());
        }
        if (query.getStartTime() != null) {
            sql.append(" AND created_time >= ?");
            params.add(query.getStartTime());
        }
        if (query.getEndTime() != null) {
            sql.append(" AND created_time <= ?");
            params.add(query.getEndTime());
        }
    }

    @Override
    public void updateForRetry(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String sql = "UPDATE ee_bus_event_entity SET error_count = 0, processing_failed_reason = '' WHERE id IN (" +
                String.join(",", Collections.nCopies(ids.size(), "?")) + ")";
        jdbcTemplate.update(sql, ids.toArray());
    }

    @Override
    public List<Map<String, Object>> countByState() {
        String sql = "SELECT processing_state as state, COUNT(*) as count FROM ee_bus_event_entity GROUP BY processing_state";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> countTrendLast24Hours() {
        String sql = "SELECT DATE_FORMAT(created_time, '%Y-%m-%d %H:%i') as time, COUNT(*) as count " +
                "FROM ee_bus_event_entity " +
                "WHERE created_time >= DATE_SUB(NOW(), INTERVAL 1 DAY) " +
                "GROUP BY time " +
                "ORDER BY time";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> subscriberLatencyRanking() {
        String sql = "SELECT successful_subscriber, AVG(TIMESTAMPDIFF(MICROSECOND, created_time, updated_time)) as avg_latency " +
                "FROM ee_bus_event_entity " +
                "WHERE processing_state = 'PROCESS_COMPLETE' " +
                "GROUP BY successful_subscriber " +
                "ORDER BY avg_latency DESC LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public BusEventEntity findById(Long id) {
        String sql = BASE_SELECT + " WHERE id = ?";
        List<BusEventEntity> list = jdbcTemplate.query(sql, new Object[]{id}, ROW_MAPPER);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void update(BusEventEntity entity) {
        String sql = "UPDATE ee_bus_event_entity SET processing_state = ?, event_data = ?, processing_failed_reason = ?, updated_time = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, entity.getProcessingState(), entity.getEventData(), entity.getProcessingFailedReason(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM ee_bus_event_entity WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<BusEventHistoryEntity> findHistoryByEventId(Long eventId) {
        String sql = "SELECT id, entity_id, status, context, create_time FROM ee_bus_event_history WHERE entity_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new Object[]{eventId}, HISTORY_ROW_MAPPER);
    }
}