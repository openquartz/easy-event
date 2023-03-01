package org.svnee.easyevent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark order subscriber
 *
 * @author svnee
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {

    /**
     * The order value.
     *
     * @return order value
     */
    int value() default Integer.MAX_VALUE;
}
