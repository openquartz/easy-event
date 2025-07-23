

package com.openquartz.easyevent.core;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

/**
 * Wraps an event that was posted, but which had no subscribers and thus could not be delivered.
 *
 * <p>Registering a DeadEvent subscriber is useful for debugging or logging, as it can detect
 * misconfigurations in a system's event distribution.
 *
 * 源代码链接自：Google Guava (com.google.common.eventbus.DeadEvent)
 * <link>https://github.com/google/guava/blob/master/guava/src/com/google/common/eventbus/DeadEvent.java</link>
 *
 * @author Cliff Biffle
 * @since 10.0
 */
public class DeadEvent {

    private final Object source;
    private final Object event;

    /**
     * Creates a new DeadEvent.
     *
     * @param source object broadcasting the DeadEvent (generally the {@link EventBus}).
     * @param event the event that could not be delivered.
     */
    public DeadEvent(Object source, Object event) {
        checkNotNull(source);
        checkNotNull(event);

        this.source = source;
        this.event = event;
    }

    /**
     * Returns the object that originated this event (<em>not</em> the object that originated the
     * wrapped event). This is generally an {@link EventBus}.
     *
     * @return the source of this event.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the wrapped, 'dead' event, which the system was unable to deliver to any registered
     * subscriber.
     *
     * @return the 'dead' event that could not be delivered.
     */
    public Object getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "DeadEvent{" +
            "source=" + source +
            ", event=" + event +
            '}';
    }
}
