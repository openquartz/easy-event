package org.svnee.easyevent.starter.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * SpringContextUtil
 *
 * @author svnee
 */
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    private SpringContextUtil() {
    }

    public static Class<?> getRealClass(Object springBean) {
        return AopUtils.isAopProxy(springBean) ? AopUtils.getTargetClass(springBean) : springBean.getClass();
    }

    /**
     * 获取 目标对象
     *
     * @param proxy 代理对象
     * @return 原对象
     */
    public static Object getTarget(Object proxy) throws Exception {
        //不是代理对象
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else { //cglib
            return getCglibProxyTargetObject(proxy);
        }
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);

        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);

        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        return ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.context = applicationContext;
    }

    /**
     * Get ioc container bean by type.
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * Get ioc container bean by name and type.
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    /**
     * Get a set of ioc container beans by type.
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }

    /**
     * Find whether the bean has annotations.
     */
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        return context.findAnnotationOnBean(beanName, annotationType);
    }

    /**
     * Get ApplicationContext.
     */
    public static ApplicationContext getInstance() {
        return context;
    }

}
