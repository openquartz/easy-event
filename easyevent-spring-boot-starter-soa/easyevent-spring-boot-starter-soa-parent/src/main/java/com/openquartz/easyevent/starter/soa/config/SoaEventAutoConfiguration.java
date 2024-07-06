package com.openquartz.easyevent.starter.soa.config;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.starter.soa.core.SoaEventCenter;
import com.openquartz.easyevent.starter.soa.core.SoaEventHandler;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventAfterAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventTransferAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * SoaEventAutoConfiguration
 *
 * @author svnee
 */
@Slf4j
@AutoConfigureBefore(EasyEventAfterAutoConfiguration.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10050)
public class SoaEventAutoConfiguration {

    public SoaEventAutoConfiguration() {
        log.info("SoaEventAutoConfiguration init >>>>>>>>>>>>>----------------");
    }

    @Bean
    public SoaEventHandler soaEventHandler(SoaEventCenter soaEventCenter, List<EventBus> eventBusList) {
        SoaEventHandler soaEventHandler = new SoaEventHandler(soaEventCenter);
        eventBusList.forEach(k -> k.register(soaEventHandler));
        return soaEventHandler;
    }

}
