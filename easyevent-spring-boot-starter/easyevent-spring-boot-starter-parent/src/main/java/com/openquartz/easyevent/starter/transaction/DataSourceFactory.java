package com.openquartz.easyevent.starter.transaction;

import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.exception.CommonErrorCode;
import com.openquartz.easyevent.common.exception.EasyEventException;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.JdbcStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotEmpty;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

@Slf4j
public class DataSourceFactory {

    public DataSource create(JdbcStorageProperties jdbcStorageProperties,
                                                     Environment environment) {

        Iterable<ConfigurationPropertySource> sources = ConfigurationPropertySources
                .get(environment);
        Binder binder = new Binder(sources);
        Properties properties = binder.bind(
                JdbcStorageProperties.PREFIX + CommonConstants.POINT_SPLITTER + CommonConstants.DATASOURCE_IDENTIFIER_STR,
                Properties.class).get();

        DataSource dataSource = buildDataSource(jdbcStorageProperties);
        buildDataSourceProperties(dataSource, properties);
        return dataSource;
    }

    @SuppressWarnings("unchecked")
    private DataSource buildDataSource(JdbcStorageProperties jdbcStorageProperties) {
        try {
            String dataSourceType = jdbcStorageProperties.getDatasource().getType();

            Class<? extends DataSource> type = (Class<? extends DataSource>) Class.forName(dataSourceType);
            String driverClassName = jdbcStorageProperties.getDatasource().getDriverClassName();
            String url = jdbcStorageProperties.getDatasource().getUrl();
            String username = jdbcStorageProperties.getDatasource().getUsername();
            String password = jdbcStorageProperties.getDatasource().getPassword();

            checkNotNull(type);
            checkNotEmpty(driverClassName);
            checkNotEmpty(url);
            checkNotEmpty(username);
            checkNotEmpty(password);

            return DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(url)
                    .username(username)
                    .password(password)
                    .type(type)
                    .build();

        } catch (ClassNotFoundException e) {
            log.error("[JdbcStorageAutoConfiguration#buildDataSource] class-not-found! error", e);
            throw new EasyEventException(CommonErrorCode.CLASS_NOT_FOUND_ERROR);
        }
    }

    private void buildDataSourceProperties(DataSource dataSource, Map<Object, Object> dsMap) {
        try {
            BeanUtils.copyProperties(dataSource, dsMap);
        } catch (Exception e) {
            log.error("[JdbcStorageAutoConfiguration#buildDataSourceProperties]error copy properties,dsMap:{}", dsMap,
                    e);
        }
    }

}
