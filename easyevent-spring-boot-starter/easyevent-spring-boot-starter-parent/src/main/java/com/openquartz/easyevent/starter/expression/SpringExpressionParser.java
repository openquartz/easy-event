package com.openquartz.easyevent.starter.expression;

import com.openquartz.easyevent.core.expression.ExpressionParser;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPEL Expression Parser
 *
 * @author svnee
 */
public class SpringExpressionParser implements ExpressionParser {

    private final SpelExpressionParser spelExpressionParser;
    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

    public SpringExpressionParser(SpelExpressionParser spelExpressionParser) {
        this.spelExpressionParser = spelExpressionParser;
    }

    @Override
    public boolean parse(String expression, Object event, Method targetMethod, Class<?> targetClass) {

        EventSubscriberRootObject root = new EventSubscriberRootObject(event, new Object[]{event});
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(root);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(targetMethod, targetClass);

        return (Boolean.TRUE.equals(getExpression(methodKey, expression).getValue(standardEvaluationContext, Boolean.class)));
    }

    private Expression getExpression(AnnotatedElementKey elementKey, String expression) {

        ExpressionKey expressionKey = createKey(elementKey, expression);
        Expression expr = conditionCache.get(expressionKey);
        if (expr == null) {
            expr = spelExpressionParser.parseExpression(expression);
            conditionCache.put(expressionKey, expr);
        }
        return expr;
    }

    private ExpressionKey createKey(AnnotatedElementKey elementKey, String expression) {
        return new ExpressionKey(elementKey, expression);
    }
}
