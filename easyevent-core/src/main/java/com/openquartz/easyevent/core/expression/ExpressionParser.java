package com.openquartz.easyevent.core.expression;

import java.lang.reflect.Method;

/**
 * Expression Parser
 * @author svnee
 */
public interface ExpressionParser {

    /**
     * Parses an expression
     * @param expression expression
     * @return parse result
     */
    boolean parse(String expression, Object event, Method targetMethod, Class<?> targetClass);

}
