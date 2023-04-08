package com.openquartz.easyevent.storage.jdbc.utils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;

/**
 * CustomerJdbcTemplate
 *
 * @author svnee
 */
@Slf4j
public class CustomerJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public CustomerJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int[] batchUpdate(final String sql, final BatchPreparedStatementSetter pss,
        final KeyHolder generatedKeyHolder) throws DataAccessException {
        return (int[]) jdbcTemplate.execute(
            conn -> conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS),
            (PreparedStatementCallback) ps -> {
                if (log.isDebugEnabled()) {
                    log.debug("Executing batch SQL update and returning generated keys [" + sql + "]");
                }
                try {
                    int batchSize = pss.getBatchSize();
                    int totalRowsAffected = 0;
                    int[] rowsAffected = new int[batchSize];
                    List generatedKeys = generatedKeyHolder.getKeyList();
                    generatedKeys.clear();
                    ResultSet keys = null;
                    for (int i = 0; i < batchSize; i++) {
                        pss.setValues(ps, i);
                        rowsAffected[i] = ps.executeUpdate();
                        totalRowsAffected += rowsAffected[i];
                        try {
                            keys = ps.getGeneratedKeys();
                            if (keys != null) {
                                RowMapper rowMapper = new ColumnMapRowMapper();
                                RowMapperResultSetExtractor rse =
                                    new RowMapperResultSetExtractor(rowMapper, 1);
                                generatedKeys.addAll(rse.extractData(keys));
                            }
                        } finally {
                            JdbcUtils.closeResultSet(keys);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "SQL batch update affected "
                                + totalRowsAffected + " rows and returned "
                                + generatedKeys.size() + " keys");
                    }
                    return rowsAffected;
                } finally {
                    if (pss instanceof ParameterDisposer) {
                        ((ParameterDisposer) pss).cleanupParameters();
                    }
                }
            });
    }
}