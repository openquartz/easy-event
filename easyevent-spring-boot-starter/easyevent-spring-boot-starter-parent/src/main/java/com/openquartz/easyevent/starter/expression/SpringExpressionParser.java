package com.openquartz.easyevent.starter.expression;

import com.openquartz.easyevent.core.expression.ExpressionParser;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPEL Expression Parser
 *
 * @author svnee
 */
public class SpringExpressionParser implements ExpressionParser, ApplicationContextAware {

    private final SpelExpressionParser spelExpressionParser;
    private ApplicationContext applicationContext;
    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

    public SpringExpressionParser(SpelExpressionParser spelExpressionParser) {
        this.spelExpressionParser = spelExpressionParser;
    }

    @Override
    public boolean parse(String expression, Object event, Method targetMethod, Class<?> targetClass) {
        EventSubscriberRootObject root = new EventSubscriberRootObject(event, applicationContext, new Object[]{event});
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(root);
        standardEvaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
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


    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
