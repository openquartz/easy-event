package com.openquartz.easyevent.common.utils;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.exception.CommonErrorCode;
import com.openquartz.easyevent.common.exception.EasyEventErrorCode;
import com.openquartz.easyevent.common.exception.EasyEventException;

/**
 * 断言工具类
 *
 * @author svnee
 */
@Slf4j
public final class Asserts {

    private Asserts() {
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrue(boolean expression, EasyEventErrorCode code) {
        isTrueIfLog(expression, null, code);
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrueIfLog(boolean expression, LogCallback logCallback, EasyEventErrorCode code) {
        if (!expression) {
            if (Objects.nonNull(logCallback)) {
                logCallback.log();
            }
            throw new EasyEventException(code);
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrueIfLog(boolean expression, LogCallback logCallback, EasyEventErrorCode code,
        Object... placeHold) {
        if (!expression) {
            if (Objects.nonNull(logCallback)) {
                logCallback.log();
            }
            throw EasyEventException.replacePlaceHold(code, placeHold);
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param code 异常码
     */
    public static void isTrue(boolean expression, EasyEventErrorCode code, Object... placeHold) {
        isTrueIfLog(expression, null, code, placeHold);
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param errorCode 异常码
     * @param exceptionClazz 异常类
     * @param <T> T 异常码
     * @param <E> E 异常
     */
    public static <T extends EasyEventErrorCode, E extends EasyEventException> void isTrueIfLog(boolean expression,
        LogCallback logCallback, T errorCode, Class<E> exceptionClazz) {
        if (!expression) {
            if (Objects.nonNull(logCallback)) {
                logCallback.log();
            }
            try {
                Constructor<E> constructor = exceptionClazz.getConstructor(errorCode.getClass());
                throw constructor.newInstance(errorCode);
            } catch (Exception ex) {
                throw new EasyEventException(CommonErrorCode.METHOD_NOT_EXIST_ERROR);
            }
        }
    }

    /**
     * 断言是否为true
     *
     * @param expression 表达式
     * @param errorCode 异常码
     * @param exceptionClazz 异常类
     * @param <T> T 异常码
     * @param <E> E 异常
     */
    public static <T extends EasyEventErrorCode, E extends EasyEventException> void isTrue(boolean expression,
        T errorCode, Class<E> exceptionClazz) {
        isTrueIfLog(expression, null, errorCode, exceptionClazz);
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param code code
     */
    public static void notNull(Object obj, EasyEventErrorCode code) {
        isTrue(Objects.nonNull(obj), code);
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param logCallback logCallback
     * @param code code
     */
    public static void notNullIfLog(Object obj, LogCallback logCallback, EasyEventErrorCode code, Object... placeHold) {
        isTrueIfLog(Objects.nonNull(obj), logCallback, code, placeHold);
    }

    /**
     * 断言非空
     *
     * @param obj obj
     * @param code code
     */
    public static void notNull(Object obj, EasyEventErrorCode code, Object... placeHold) {
        isTrue(Objects.nonNull(obj), code, placeHold);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNull(Object obj, EasyEventErrorCode code) {
        isTrue(Objects.isNull(obj), code);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNull(Object obj, EasyEventErrorCode code, Object... placeHold) {
        isTrue(Objects.isNull(obj), code, placeHold);
    }

    /**
     * 断言为空
     *
     * @param obj obj
     * @param code code
     */
    public static void isNullIfLog(Object obj, LogCallback logCallback, EasyEventErrorCode code, Object... placeHold) {
        isTrueIfLog(Objects.isNull(obj), logCallback, code, placeHold);
    }

    /**
     * assert string not empty
     *
     * @param obj obj string
     * @param code code
     * @param placeHold place-hold
     */
    public static void notEmpty(String obj, EasyEventErrorCode code, Object... placeHold) {
        isTrue(StringUtils.isNotBlank(obj), code, placeHold);
    }

    /**
     * assert string not empty
     *
     * @param obj obj string
     * @param logCallback log
     * @param code code
     * @param placeHold place-hold
     */
    public static void notEmptyIfLog(String obj, LogCallback logCallback, EasyEventErrorCode code,
        Object... placeHold) {
        isTrueIfLog(StringUtils.isNotBlank(obj), logCallback, code, placeHold);
    }

    /**
     * assert collection not empty
     *
     * @param obj obj collection
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmpty(Collection<?> obj, EasyEventErrorCode code, Object... placeHold) {
        isTrue(CollectionUtils.isNotEmpty(obj), code, placeHold);
    }

    /**
     * assert map not empty
     *
     * @param obj obj collection
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmpty(Map<?, ?> obj, EasyEventErrorCode code, Object... placeHold) {
        isTrue(obj != null && !obj.isEmpty(), code, placeHold);
    }

    /**
     * assert map not empty
     *
     * @param obj obj collection
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmptyIfLog(Map<?, ?> obj, LogCallback logCallback, EasyEventErrorCode code,
        Object... placeHold) {
        isTrueIfLog(obj != null && !obj.isEmpty(), logCallback, code, placeHold);
    }

    /**
     * assert collection not empty
     *
     * @param obj obj collection
     * @param logCallback callback
     * @param code error code
     * @param placeHold place-hold
     */
    public static void notEmptyIfLog(Collection<?> obj, LogCallback logCallback, EasyEventErrorCode code,
        Object... placeHold) {
        isTrueIfLog(CollectionUtils.isNotEmpty(obj), logCallback, code, placeHold);
    }

}