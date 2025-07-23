package com.openquartz.easyevent.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an event subscriber method as being thread-safe. This annotation indicates that EventBus
 * may invoke the event subscriber simultaneously from multiple threads.
 *
 * <p>This does not mark the method, and so should be used in combination with {@link Subscribe}.
 *
 * 源代码链接自：Google Guava (com.google.common.eventbus.AllowConcurrentEvents)
 * <link>https://github.com/google/guava/blob/master/guava/src/com/google/common/eventbus/AllowConcurrentEvents.java</link>
 *
 * @author svnee
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AllowConcurrentEvents {}
