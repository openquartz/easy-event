package org.svnee.easyevent.starter.spring.boot.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * EasyEventStorageAutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@AutoConfigureAfter(EasyEventCommonAutoConfiguration.class)
public class EasyEventStorageAutoConfiguration {


}
