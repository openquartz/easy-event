package com.openquartz.easyevent.starter.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptor;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptorCenter;
import com.openquartz.easyevent.core.intreceptor.PublisherInterceptor;
import com.openquartz.easyevent.core.intreceptor.PublisherInterceptorChain;
import com.openquartz.easyevent.core.intreceptor.TriggerInterceptor;
import com.openquartz.easyevent.core.intreceptor.TriggerInterceptorChain;
import com.openquartz.easyevent.starter.utils.SpringContextUtil;

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
