package org.svnee.easyevent.starter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.svnee.easyevent.common.constant.CommonConstants;

/**
 * 开启EasyEvent auto configuration
 *
 * @author svnee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@ComponentScans(value = {@ComponentScan(CommonConstants.BASE_PACKAGE_PATH)})
public @interface EnableEasyEventAutoConfiguration {

}
