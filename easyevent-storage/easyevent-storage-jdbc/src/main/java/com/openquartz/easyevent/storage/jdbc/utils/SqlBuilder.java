package com.openquartz.easyevent.storage.jdbc.utils;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotEmpty;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.exception.CommonErrorCode;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.utils.Asserts;

/**
 * @author svnee
 **/
public class SqlBuilder {

    private String table;
    private String idColumn = "id";
    private TypeDefault typeDefault = TypeDefault.DEFAULT;
    private final Map<String, Pair<Class<?>, Object>> columnMap = new LinkedHashMap<>();
    private final Map<String, Object> defaultColumnMap = new LinkedHashMap<>();

    private SqlBuilder() {
    }

    private SqlBuilder(String table) {
        this.table = table;
    }

    public static SqlBuilder builder(String table) {

        checkNotEmpty(table);

        return new SqlBuilder(table);
    }

    public SqlBuilder idName(String column) {

        checkNotEmpty(column);

        idColumn = column;
        return this;
    }

    public SqlBuilder columnDefault(String column, Object value) {

        checkNotNull(value);

        Asserts.isTrue(columnMap.containsKey(column), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        Asserts.isTrue(columnMap.get(column).getKey().isAssignableFrom(value.getClass()),
            CommonErrorCode.PARAM_ILLEGAL_ERROR);

        defaultColumnMap.put(column, value);
        return this;
    }

    public SqlBuilder typeDefault(TypeDefault typeDefault) {
        this.typeDefault = typeDefault;
        return this;
    }

    public SqlBuilder column(String column, Class<?> valType, Object value) {

        checkNotNull(valType);
        checkNotEmpty(column);

        if (Objects.nonNull(value)) {
            Asserts.isTrue(valType.isAssignableFrom(value.getClass()), CommonErrorCode.PARAM_ILLEGAL_ERROR);
        }
        columnMap.put(column, Pair.of(valType, value));
        return this;
    }

    private Object getDefaultVal(String column) {
        if (Objects.nonNull(defaultColumnMap.get(column))) {
            return defaultColumnMap.get(column);
        }
        return typeDefault.matchValue(columnMap.get(column).getKey());
    }

    private boolean isSkipIdNullable(String column) {
        return column.equals(idColumn) && columnMap.get(idColumn).getValue() == null;
    }

    public String insertSelective() {

        checkNotEmpty(columnMap);

        StringJoiner columnSql = new StringJoiner(CommonConstants.COMMA);
        StringJoiner applyColumnSql = new StringJoiner(CommonConstants.COMMA);

        columnMap.entrySet().stream()
            .filter(e -> !isSkipIdNullable(e.getKey()))
            .filter(e -> Objects.nonNull(e.getValue().getValue()) || Objects.nonNull(getDefaultVal(e.getKey())))
            .forEach(k -> {
                columnSql.add(k.getKey());
                Object value =
                    Objects.nonNull(k.getValue().getValue()) ? k.getValue().getValue() : getDefaultVal(k.getKey());
                applyColumnSql.add(String.valueOf(value));
            });

        return "insert into " + table + " (" + columnSql + ") values (" + applyColumnSql + ")";
    }

}

