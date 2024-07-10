package com.openquartz.easyevent.storage.model;

import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.common.utils.reflect.ReflectionUtils;
import com.openquartz.easyevent.storage.constants.EventConstant;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Optional;

@Data
public class EventBody<T> {

    private T event;

    private EventContext context;

    public EventBody() {
    }

    public EventBody(T event, EventContext context) {
        this.event = event;
        this.context = context;
    }

    public String computeEventKey() {

        try {
            Method method = ReflectionUtils.findMethod(event.getClass(), EventConstant.EVENT_KEY_METHOD);

            return Optional.ofNullable(method)
                    .map(e -> {
                        try {
                            return e.invoke(event);
                        } catch (Exception ex) {
                            return null;
                        }
                    })
                    .map(String::valueOf)
                    .orElse(StringUtils.EMPTY);
        } catch (Exception ex) {
            return StringUtils.EMPTY;
        }
    }
}
