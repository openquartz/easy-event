package com.openquartz.easyevent.starter.spring.boot.autoconfig;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * EasyEventStorageAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@AutoConfigureAfter(EasyEventCommonAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 100)
public class EasyEventStorageAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info(
            "-----------------------------------------EasyEventStorageAutoConfiguration-------------------------------");
    }

}
