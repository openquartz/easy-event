

package com.openquartz.easyevent.core.annotation;

import com.openquartz.easyevent.core.EventBus;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event subscriber.
 *
 * <p>The type of event will be indicated by the method's first (and only) parameter, which cannot
 * be primitive. If this annotation is applied to methods with zero parameters, or more than one
 * parameter, the object containing the method will not be able to register for event delivery from
 * the {@link EventBus}.
 *
 * <p>Unless also annotated with @{@link AllowConcurrentEvents}, event subscriber methods will be
 * invoked serially by each event bus that they are registered with.
 *
 * 源代码链接自：Google Guava (com.google.common.eventbus.Subscribe)
 * <link>https://github.com/google/guava/blob/master/guava/src/com/google/common/eventbus/Subscribe.java</link>
 *
 *
 * @author svnee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * 符合条件
     */
    String condition() default "";

    /**
     * 加入到主事务中
     */
    boolean joinTransaction() default true;
}
