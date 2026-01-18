package com.openquartz.easyevent.starter.spring.boot.autoconfig;

import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.JdbcStorageProperties;
import com.openquartz.easyevent.starter.transaction.DataSourceFactory;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.identify.IdGenerator;
import com.openquartz.easyevent.storage.jdbc.JdbcEventStorageServiceImpl;
import com.openquartz.easyevent.storage.jdbc.identify.SnowflakeIdGenerator;
import com.openquartz.easyevent.storage.jdbc.mapper.BusEventEntityMapper;
import com.openquartz.easyevent.storage.jdbc.mapper.impl.BusEventEntityMapperImpl;
import com.openquartz.easyevent.storage.jdbc.sharding.ShardingRouter;
import com.openquartz.easyevent.storage.jdbc.sharding.impl.DefaultShardingRouterImpl;
import com.openquartz.easyevent.storage.jdbc.sharding.property.EventStorageShardingProperty;
import com.openquartz.easyevent.storage.jdbc.table.EasyEventTableGeneratorSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Jdbc Storage Configuration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(JdbcStorageProperties.class)
@ConditionalOnClass(JdbcEventStorageServiceImpl.class)
@AutoConfigureAfter(EasyEventStorageAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 120)
public class JdbcStorageAutoConfiguration {

    public JdbcStorageAutoConfiguration(){
        log.info("JdbcStorageAutoConfiguration init >>>>>>>>>--------------------");
    }

    @Bean
    @ConditionalOnMissingBean(type = "jdbcStorageJdbcTemplate", value = JdbcTemplate.class)
    public JdbcTemplate jdbcStorageJdbcTemplate(JdbcStorageProperties jdbcStorageProperties,
                                                Environment environment) {
        return new JdbcTemplate(DataSourceFactory.newInstance().create(jdbcStorageProperties, environment));
    }

    @Bean
    @ConditionalOnMissingBean(type = "busEventEntityMapperImpl", value = BusEventEntityMapper.class)
    public BusEventEntityMapper busEventEntityMapperImpl(
            @Qualifier("jdbcStorageJdbcTemplate") JdbcTemplate jdbcStorageJdbcTemplate,
            EasyEventTableGeneratorSupplier easyEventTableGeneratorSupplier) {
        return new BusEventEntityMapperImpl(jdbcStorageJdbcTemplate, easyEventTableGeneratorSupplier);
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator() {
        return new SnowflakeIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShardingRouter shardingRouter(JdbcStorageProperties jdbcStorageProperties,
                                         @Autowired(required = false) IdGenerator idGenerator) {
        EventStorageShardingProperty shardingProperty = new EventStorageShardingProperty();
        shardingProperty.setTotalSharding(jdbcStorageProperties.getTable().getTotalSharding());
        return new DefaultShardingRouterImpl(shardingProperty, idGenerator);
    }

    @Bean
    public EasyEventTableGeneratorSupplier easyEventTableGeneratorSupplier(JdbcStorageProperties jdbcStorageProperties,
                                                                           ShardingRouter shardingRouter) {
        return new EasyEventTableGeneratorSupplier(jdbcStorageProperties.getTable().getPrefix(), shardingRouter);
    }

    @Bean
    @ConditionalOnMissingBean(type = "jdbcEventStorageService", value = EventStorageService.class)
    public EventStorageService jdbcEventStorageService(BusEventEntityMapper busEventEntityMapperImpl,
                                                       Serializer serializer,
                                                       @Autowired(required = false) IdGenerator idGenerator,
                                                       EasyEventProperties easyEventProperties) {

        return new JdbcEventStorageServiceImpl(busEventEntityMapperImpl, serializer, idGenerator, easyEventProperties);
    }

}
