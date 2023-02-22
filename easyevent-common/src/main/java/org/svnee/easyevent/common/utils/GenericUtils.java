package org.svnee.easyevent.common.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型工具类
 *
 * @author svnee
 */
public final class GenericUtils {

    private GenericUtils() {
    }

    /**
     * 获取接口上的泛型T
     */
    public static Class<?>[] getGenericInterfaceParameter(Class<?> clazz, Class<?> interfaceType) {
        Class<?>[] result = new Class<?>[0];
        // make sure interfaceType is a generic interface.
        if (!interfaceType.isInterface() || interfaceType.getTypeParameters().length < 1) {
            return result;
        }
        // get all interfaces implemented by concrete class.
        Type[] interfaceTypes = clazz.getGenericInterfaces();

        // for each interface the concrete class implements
        // we check if that interface is actually equal to interfaceType
        // and is a parametrized type,
        // i.e has a type parameter.
        // Once a match is found, we return the type parameters.
        for (Type it : interfaceTypes) {
            if (it instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) it;
                if (!parameterizedType.getRawType().equals(interfaceType)) {
                    continue;
                }
                Type[] typeParameters = parameterizedType.getActualTypeArguments();
                result = new Class[typeParameters.length];
                for (int j = 0; j < typeParameters.length; j++) {
                    result[j] = (Class<?>) typeParameters[j];
                }
            }
        }

        return result;
    }


    /**
     * 获取类上的泛型T
     *
     * @param realClass realClass
     * @param index 泛型索引
     */
    public static Class<?> getClassT(Class<?> realClass, int index) {
        Type type = realClass.getGenericSuperclass();
        return getaClass(type, index);
    }

    private static Class<?> checkType(Type type, int index) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else {
            return getaClass(type, index);
        }
    }

    private static Class<?> getaClass(Type type, int index) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type t = pt.getActualTypeArguments()[index];
            return checkType(t, index);
        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType"
                + ", but <" + type + "> is of type " + className);
        }
    }

}
