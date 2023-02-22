package org.svnee.easyevent.starter.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.svnee.easyevent.core.intreceptor.HandlerInterceptor;
import org.svnee.easyevent.core.intreceptor.HandlerInterceptorCenter;
import org.svnee.easyevent.core.intreceptor.PublisherInterceptor;
import org.svnee.easyevent.core.intreceptor.PublisherInterceptorChain;
import org.svnee.easyevent.core.intreceptor.TriggerInterceptor;
import org.svnee.easyevent.core.intreceptor.TriggerInterceptorChain;
import org.svnee.easyevent.starter.utils.SpringContextUtil;

/**
 * InterceptorPostProcessor
 *
 * @author svnee
 **/
public class InterceptorPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof TriggerInterceptor) {
            TriggerInterceptor triggerInterceptor = (TriggerInterceptor) bean;
            TriggerInterceptorChain.addInterceptor(triggerInterceptor);
        }
        if (bean instanceof PublisherInterceptor) {
            PublisherInterceptor publisherInterceptor = (PublisherInterceptor) bean;
            PublisherInterceptorChain.addInterceptor(publisherInterceptor);
        }
        if (bean instanceof HandlerInterceptor) {
            Class<?> realClass = SpringContextUtil.getRealClass(bean);
            HandlerInterceptorCenter.addInterceptor((HandlerInterceptor<?>) bean, realClass);
        }
        return bean;
    }
}
