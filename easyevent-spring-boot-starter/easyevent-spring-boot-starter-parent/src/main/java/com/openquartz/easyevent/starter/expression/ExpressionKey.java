package com.openquartz.easyevent.starter.expression;

import org.springframework.context.expression.AnnotatedElementKey;

import java.util.Objects;

public class ExpressionKey {

    private final AnnotatedElementKey annotatedElementKey;
    private final String expression;

    public ExpressionKey(AnnotatedElementKey annotatedElementKey, String expression) {
        this.annotatedElementKey = annotatedElementKey;
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpressionKey that = (ExpressionKey) o;
        return Objects.equals(annotatedElementKey, that.annotatedElementKey) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotatedElementKey, expression);
    }
}
