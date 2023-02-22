package org.svnee.easyevent.starter.processor;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.svnee.easyevent.core.EventBus;
import org.svnee.easyevent.starter.annotation.EventHandler;
import org.svnee.easyevent.starter.utils.SpringContextUtil;

/**
 * EventHandler( mark {@link org.svnee.easyevent.starter.annotation.EventHandler} spring bean) PostProcessor
 *
 * @author svnee
 **/
public class EventHandlerPostProcessor implements BeanPostProcessor {

    private final List<EventBus> eventBusList;

    public EventHandlerPostProcessor(List<EventBus> eventBusList) {
        this.eventBusList = eventBusList;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> realClass = SpringContextUtil.getRealClass(bean);
        if (getAnnotatedEventHandler(realClass) != null) {
            register(bean);
        }
        return bean;
    }

    private void register(Object bean) {
        for (EventBus eventBus : eventBusList) {
            eventBus.register(bean);
        }
    }

    private static EventHandler getAnnotatedEventHandler(Class<?> clazz) {
        EventHandler handler = clazz.getDeclaredAnnotation(EventHandler.class);
        if (Objects.nonNull(handler)) {
            return handler;
        }
        return AnnotatedElementUtils.findMergedAnnotation(clazz, EventHandler.class);
    }
}
