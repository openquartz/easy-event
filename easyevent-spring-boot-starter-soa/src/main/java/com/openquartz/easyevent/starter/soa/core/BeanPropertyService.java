package com.openquartz.easyevent.starter.soa.core;

import com.openquartz.easyevent.common.utils.ExceptionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
public class BeanPropertyService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public Object getProperty(String beanName, String propertyName) {
        try {
            return applicationContext.getBean(beanName).getClass().getMethod("get" + StringUtils.capitalize(propertyName)).invoke(applicationContext.getBean(beanName));
        } catch (Exception ex) {
            return ExceptionUtils.rethrow(ex);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
