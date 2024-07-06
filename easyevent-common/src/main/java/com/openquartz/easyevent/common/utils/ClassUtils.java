package com.openquartz.easyevent.common.utils;

/**
 * ClassUtils
 *
 * @author svnee
 */
public class ClassUtils {

    private ClassUtils() {
    }

    public static Class<?> getRealClass(Object bean) {

        if (bean == null) {
            throw new IllegalArgumentException("bean is null!");
        }

        Class<?> beanClass = bean.getClass();
        if (beanClass.getName().contains("EnhancerBySpringCGLIB")) {
            return beanClass.getSuperclass();
        }
        return beanClass;
    }

}
