package org.svnee.easyevent.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.common.concurrent.lock.DistributedLockSupport;
import org.svnee.easyevent.common.concurrent.lock.impl.LockSupportImpl;
import org.svnee.easyevent.starter.init.EasyEventInitializingEntrance;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import org.svnee.easyevent.starter.transaction.SpringTransactionSupport;

/**
 * EasyEventCommonAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@AutoConfigureAfter(TransactionAutoConfiguration.class)
@EnableConfigurationProperties(EasyEventCommonProperties.class)
public class EasyEventCommonAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public EasyEventInitializingEntrance easyEventInitializingEntrance(
        EasyEventCommonProperties easyEventCommonProperties) {
        return new EasyEventInitializingEntrance(easyEventCommonProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionSupport transactionSupport(@Autowired(required = false) TransactionTemplate transactionTemplate) {
        return new SpringTransactionSupport(transactionTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer() {
        return Serializer.DEFAULT;
    }

    @Bean
    @ConditionalOnMissingBean
    public LockSupport lockSupport(@Autowired(required = false) DistributedLockSupport distributedLockSupport) {
        return new LockSupportImpl(distributedLockSupport);
    }

}
