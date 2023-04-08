package com.openquartz.easyevent.storage.jdbc.mapper.impl;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotEmpty;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.storage.jdbc.table.EasyEventTableGeneratorSupplier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.lang.NonNull;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.DataUtils;
import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.common.utils.Joiner;
import com.openquartz.easyevent.common.utils.MapUtils;
import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.jdbc.constant.JdbcTemplateConstants;
import com.openquartz.easyevent.storage.jdbc.mapper.BusEventEntityMapper;
import com.openquartz.easyevent.storage.jdbc.utils.CustomerJdbcTemplate;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.BusEventEntity;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;
import com.openquartz.easyevent.storage.model.EventLifecycleState;

/**
 * BusEventEntityMapper
 *
 * @author svnee
 **/
public class BusEventEntityMapperImpl implements BusEventEntityMapper {

    private final JdbcTemplate jdbcTemplate;
    private final CustomerJdbcTemplate customerJdbcTemplate;
    private final EasyEventTableGeneratorSupplier supplier;

    public BusEventEntityMapperImpl(JdbcTemplate jdbcTemplate,
        EasyEventTableGeneratorSupplier supplier) {

        checkNotNull(jdbcTemplate);
        checkNotNull(supplier);

        this.jdbcTemplate = jdbcTemplate;
        this.customerJdbcTemplate = new CustomerJdbcTemplate(jdbcTemplate);
        this.supplier = supplier;
    }

    private static final String GET_SUCCESS_SUBSCRIBER_SQL = "select successful_subscriber from {0} where id = ?";
    private static final String GET_ALL_SQL = "select app_id,source_id,class_name,error_count,successful_subscriber,processing_state,trace_id,event_data,creating_owner,processing_owner,processing_available_date,processing_failed_reason,created_time,updated_time,id from {0} ";
    private static final String GET_BASE_SQL = "select app_id,source_id,class_name,error_count,successful_subscriber,processing_state,id from {0} where id =?";

    private static final String INSERT_SQL = "insert into {0}(app_id,source_id,class_name,error_count,successful_subscriber,processing_state,trace_id,event_data,creating_owner,processing_owner,processing_available_date,processing_failed_reason,created_time,updated_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_SQL_WITH_ID = "insert into {0}(app_id,source_id,class_name,error_count,successful_subscriber,processing_state,trace_id,event_data,creating_owner,processing_owner,processing_available_date,processing_failed_reason,created_time,updated_time,id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String REFRESH_SOURCE_SQL = "update {0} set source_id = :sourceId where id = :entityId";

    private static final String REFRESH_PROCESS_STATE_SQL = "update {0} set processing_state=?,processing_failed_reason=? where id = ?";
    private static final String BATCH_REFRESH_PROCESS_STATE_SQL = "update {0} set processing_state= :processingState ,processing_failed_reason= :processingFailedReason where id in (:idList)";

    private static final String REFRESH_START_PROCESSING_SQL = "update {0} set processing_state=?,processing_failed_reason=?,processing_owner=? where id = ?";
    private static final String REFRESH_SEND_FAILED_SQL = "update {0} set processing_state=?,processing_failed_reason=?,processing_owner=?,error_count=error_count+1 where id = ?";
    private static final String REFRESH_PROCESSING_FAILED_SQL = "update {0} set processing_state=?,processing_failed_reason=?,successful_subscriber=?,error_count=error_count+1 where id = ?";

    private static final int PROCESS_FAIL_REASON_LENGTH = 128;

    @Override
    public void insertSelective(BusEventEntity busEventEntity) {

        checkNotNull(busEventEntity);

        if (Objects.nonNull(busEventEntity.getEntityId())) {
            insertListWithSupplierId(Collections.singletonList(busEventEntity));
        } else {
            insertList(Collections.singletonList(busEventEntity));
        }
    }

    @Override
    public void refreshSourceId(Long entityId, Long sourceId) {

        Map<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("sourceId", sourceId);
        paramMap.put("entityId", entityId);

        String sql = MessageFormat
            .format(REFRESH_SOURCE_SQL, supplier.genBusEventEntityTable(entityId));

        int actual = new NamedParameterJdbcTemplate(jdbcTemplate).update(sql, paramMap);
        DataUtils.checkUpdateOne(actual);
    }

    @Override
    public void insertList(List<BusEventEntity> entityList) {

        String sql = MessageFormat.format(INSERT_SQL, supplier.genBusEventEntityTable());

        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        customerJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                BusEventEntity entity = entityList.get(i);
                ps.setString(1, entity.getAppId());
                ps.setLong(2, Objects.nonNull(entity.getSourceId()) ? entity.getSourceId() : 0);
                ps.setString(3, entity.getClassName());
                ps.setInt(4, entity.getErrorCount());
                ps.setString(5, Joiner.join(entity.getSuccessfulSubscriberList(), CommonConstants.COMMA));
                ps.setString(6, entity.getProcessingState().getCode());
                ps.setString(7, entity.getTraceId());
                ps.setString(8, entity.getEventData());
                ps.setString(9, entity.getCreatingOwner());
                ps.setString(10, entity.getProcessingOwner());
                ps.setDate(11, Objects.nonNull(entity.getProcessingAvailableDate()) ? new java.sql.Date(
                    entity.getProcessingAvailableDate().getTime()) : null);
                ps.setString(12, entity.getProcessingFailedReason());
                ps.setTimestamp(13,
                    Objects.nonNull(entity.getCreatedTime()) ? new Timestamp(entity.getCreatedTime().getTime())
                        : null);
                ps.setTimestamp(14,
                    Objects.nonNull(entity.getUpdatedTime()) ? new Timestamp(entity.getUpdatedTime().getTime())
                        : null);
            }

            @Override
            public int getBatchSize() {
                return entityList.size();
            }
        }, generatedKeyHolder);

        List<Map<String, Object>> objectMapList = generatedKeyHolder.getKeyList();
        for (int i = 0; i < objectMapList.size(); i++) {
            Map<String, Object> map = objectMapList.get(i);
            Long id = ((Number) map.get(JdbcTemplateConstants.GENERATED_KEY)).longValue();
            entityList.get(i).setEntityId(id);
        }
    }

    @Override
    public void insertListWithSupplierId(List<BusEventEntity> entityList) {

        Map<String, List<BusEventEntity>> shardingTable2EntityMap = entityList.stream().collect(Collectors
            .groupingBy(e -> supplier.genBusEventEntityTable(e.getEntityId()),
                Collectors.toList()));

        shardingTable2EntityMap.forEach((table, data) -> {

            String sql = MessageFormat.format(INSERT_SQL_WITH_ID, table);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    BusEventEntity entity = data.get(i);
                    ps.setString(1, entity.getAppId());
                    ps.setLong(2, Objects.nonNull(entity.getSourceId()) ? entity.getSourceId() : 0);
                    ps.setString(3, entity.getClassName());
                    ps.setInt(4, entity.getErrorCount());
                    ps.setString(5, Joiner.join(entity.getSuccessfulSubscriberList(), CommonConstants.COMMA));
                    ps.setString(6, entity.getProcessingState().getCode());
                    ps.setString(7, entity.getTraceId());
                    ps.setString(8, entity.getEventData());
                    ps.setString(9, entity.getCreatingOwner());
                    ps.setString(10, entity.getProcessingOwner());
                    ps.setTimestamp(11, Objects.nonNull(entity.getProcessingAvailableDate()) ?
                        new Timestamp(entity.getProcessingAvailableDate().getTime()) : null);
                    ps.setString(12, entity.getProcessingFailedReason());
                    ps.setTimestamp(13,
                        Objects.nonNull(entity.getCreatedTime()) ? new Timestamp(entity.getCreatedTime().getTime())
                            : null);
                    ps.setTimestamp(14,
                        Objects.nonNull(entity.getUpdatedTime()) ? new Timestamp(entity.getUpdatedTime().getTime())
                            : null);
                    ps.setLong(15, entity.getEntityId());
                }

                @Override
                public int getBatchSize() {
                    return data.size();
                }
            });
        });
    }

    @Override
    public void refreshSendComplete(EventId eventId, EventLifecycleState transferSuccess) {

        checkNotNull(eventId);
        checkNotNull(transferSuccess);

        refreshProcessState(eventId, transferSuccess, StringUtils.EMPTY);
    }

    @Override
    public void batchRefreshSendComplete(List<EventId> eventIdList, EventLifecycleState transferSuccess) {

        checkNotEmpty(eventIdList);
        checkNotNull(transferSuccess);

        Map<String, List<Long>> shardingTable2EntityIdMap = eventIdList.stream()
            .collect(Collectors.groupingBy(e -> supplier.genBusEventEntityTable(e.getId()),
                Collectors.mapping(EventId::getId, Collectors.toList())));

        shardingTable2EntityIdMap.forEach((k, v) -> {
            String sql = MessageFormat.format(BATCH_REFRESH_PROCESS_STATE_SQL, k);

            Map<String, Object> paramMap = MapUtils.newHashMapWithExpectedSize(3);
            paramMap.put("processingState", transferSuccess.getCode());
            paramMap.put("idList", v);
            paramMap.put("processingFailedReason", StringUtils.EMPTY);

            new NamedParameterJdbcTemplate(jdbcTemplate).update(sql, paramMap);
        });
    }

    @Override
    public void refreshSendFailed(EventId eventId, EventLifecycleState transferFailed, Exception ex) {

        checkNotNull(eventId);
        checkNotNull(transferFailed);

        String sql = MessageFormat
            .format(REFRESH_SEND_FAILED_SQL, supplier.genBusEventEntityTable(eventId.getId()));

        String processFailedReason = Objects.nonNull(ex) ? ex.getMessage() : StringUtils.EMPTY;
        processFailedReason = StringUtils.splitPrefix(processFailedReason, PROCESS_FAIL_REASON_LENGTH);
        int actual = jdbcTemplate
            .update(sql, transferFailed.getCode(), processFailedReason, IpUtil.getIp(), eventId.getId());
        DataUtils.checkUpdateOne(actual);
    }

    private void refreshProcessState(EventId eventId, EventLifecycleState state, String processFailedReason) {

        String sql = MessageFormat
            .format(REFRESH_PROCESS_STATE_SQL, supplier.genBusEventEntityTable(eventId.getId()));
        int actual = jdbcTemplate
            .update(sql, state.getCode(), StringUtils.splitPrefix(processFailedReason, PROCESS_FAIL_REASON_LENGTH),
                eventId.getId());
        DataUtils.checkUpdateOne(actual);
    }

    @Override
    public void refreshStartProcessing(EventId eventId, EventLifecycleState startProcessing) {

        checkNotNull(eventId);
        checkNotNull(startProcessing);

        String sql = MessageFormat
            .format(REFRESH_START_PROCESSING_SQL, supplier.genBusEventEntityTable(eventId.getId()));

        int actual = jdbcTemplate
            .update(sql, startProcessing.getCode(), StringUtils.EMPTY, IpUtil.getIp(), eventId.getId());
        DataUtils.checkUpdateOne(actual);
    }

    @Override
    public void processingComplete(EventId eventId, EventLifecycleState processComplete) {

        checkNotNull(eventId);
        checkNotNull(processComplete);

        refreshProcessState(eventId, processComplete, StringUtils.EMPTY);
    }

    @Override
    public void processingFailed(EventId eventId, EventLifecycleState processFailed,
        List<String> successSubscriberIdentifyList, Exception invokeError) {

        checkNotNull(eventId);

        String successfulSubscriber = Joiner.join(successSubscriberIdentifyList, CommonConstants.COMMA);
        String sql = MessageFormat
            .format(REFRESH_PROCESSING_FAILED_SQL, supplier.genBusEventEntityTable(eventId.getId()));

        String failReason = Objects.nonNull(invokeError) ? invokeError.getMessage() : StringUtils.EMPTY;
        failReason = StringUtils.splitPrefix(failReason, PROCESS_FAIL_REASON_LENGTH);
        int affect = jdbcTemplate.update(sql, processFailed.getCode(),
            failReason,
            successfulSubscriber,
            eventId.getId());
        DataUtils.checkUpdateOne(affect);
    }

    @Override
    public List<String> getSuccessfulSubscriberIdentify(EventId eventId) {
        // SQL
        String sql = MessageFormat
            .format(GET_SUCCESS_SUBSCRIBER_SQL, supplier.genBusEventEntityTable(eventId.getId()));

        String successfulSubscriber = jdbcTemplate.queryForObject(sql, String.class, eventId.getId());
        return Joiner.split(successfulSubscriber, CommonConstants.COMMA);
    }

    @Override
    public BaseEventEntity getBaseEntity(EventId eventId) {

        checkNotNull(eventId);

        String sql = MessageFormat.format(GET_BASE_SQL, supplier.genBusEventEntityTable(eventId.getId()));
        List<BaseEventEntity> entityList = jdbcTemplate.query(sql, (rs, rowNum) -> {
            BaseEventEntity baseEventEntity = new BaseEventEntity();
            baseEventEntity.setEntityId(rs.getLong("id"));
            baseEventEntity.setAppId(rs.getString("app_id"));
            baseEventEntity.setSourceId(rs.getLong("source_id"));
            baseEventEntity.setClassName(rs.getString("class_name"));
            baseEventEntity.setErrorCount(rs.getInt("error_count"));
            baseEventEntity.setSuccessfulSubscriberList(
                Joiner.split(rs.getString("successful_subscriber"), CommonConstants.COMMA));
            baseEventEntity.setProcessingState(EventLifecycleState.of(rs.getString("processing_state")));
            return baseEventEntity;
        }, eventId.getId());
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }
        return entityList.get(0);
    }

    @Override
    public List<BusEventEntity> getBySelectiveCondition(BusEventSelectorCondition condition) {

        checkNotNull(condition);

        List<String> shardingTableList = supplier.genAllBusEventEntityTableList();

        List<BusEventEntity> resultList = new ArrayList<>();
        for (String shardingTable : shardingTableList) {

            String sql = MessageFormat.format(GET_ALL_SQL, shardingTable);
            StringJoiner joiner = new StringJoiner(" and ");

            Map<String, Object> paramMap = MapUtils.newHashMap();
            if (CollectionUtils.isNotEmpty(condition.getLifecycleStateList())) {
                List<String> lifecycleStateList = condition.getLifecycleStateList().stream()
                    .map(EventLifecycleState::getCode)
                    .collect(Collectors.toList());

                joiner.add("processing_state in (:processingState)");
                paramMap.put("processingState", lifecycleStateList);
            }
            if (CollectionUtils.isNotEmpty(condition.getCreatingOwnerList())) {
                joiner.add("creating_owner in (:creatingOwner)");
                paramMap.put("creatingOwner", condition.getCreatingOwnerList());
            }
            if (Objects.nonNull(condition.getCreateTimeRange())) {
                if (Objects.nonNull(condition.getCreateTimeRange().getStart())) {
                    joiner.add("created_time >= :startCreateTime");
                    paramMap.put("startCreateTime", new Timestamp(condition.getCreateTimeRange().getStart().getTime()));
                }
                if (Objects.nonNull(condition.getCreateTimeRange().getEnd())) {
                    joiner.add("created_time < :endCreateTime");
                    paramMap.put("endCreateTime", new Timestamp(condition.getCreateTimeRange().getEnd().getTime()));
                }
            }
            if (Objects.nonNull(condition.getMaxErrorCount())) {
                joiner.add("error_count <= :maxErrorCount");
                paramMap.put("maxErrorCount", condition.getMaxErrorCount());
            }
            if (Objects.nonNull(condition.getMinErrorCount())) {
                joiner.add("error_count >= :minErrorCount");
                paramMap.put("minErrorCount", condition.getMinErrorCount());
            }
            if (joiner.length() > 0) {
                sql = sql + " where " + joiner;
            }
            if (Objects.nonNull(condition.getOffset())) {
                sql = sql + " limit :offset";
                paramMap.put("offset", condition.getOffset());
            }

            List<BusEventEntity> entityList = new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(sql, paramMap, new BusEventEntityRowMapper());
            resultList.addAll(entityList);
        }
        return resultList;
    }

    public static class BusEventEntityRowMapper implements RowMapper<BusEventEntity> {

        @Override
        public BusEventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            BusEventEntity busEventEntity = new BusEventEntity();
            busEventEntity.setTraceId(rs.getString("trace_id"));
            busEventEntity.setEventData(rs.getString("event_data"));
            busEventEntity.setCreatingOwner(rs.getString("creating_owner"));
            busEventEntity.setProcessingOwner(rs.getString("processing_owner"));
            busEventEntity.setProcessingAvailableDate(rs.getTimestamp("processing_available_date"));
            busEventEntity.setProcessingFailedReason(rs.getString("processing_failed_reason"));
            busEventEntity.setCreatedTime(rs.getTimestamp("created_time"));
            busEventEntity.setUpdatedTime(rs.getTimestamp("updated_time"));
            busEventEntity.setEntityId(rs.getLong("id"));
            busEventEntity.setAppId(rs.getString("app_id"));
            busEventEntity.setSourceId(rs.getLong("source_id"));
            busEventEntity.setClassName(rs.getString("class_name"));
            busEventEntity.setErrorCount(rs.getInt("error_count"));
            busEventEntity.setSuccessfulSubscriberList(
                Joiner.split(rs.getString("successful_subscriber"), CommonConstants.COMMA));
            busEventEntity.setProcessingState(EventLifecycleState.of(rs.getString("processing_state")));
            return busEventEntity;
        }
    }
}
