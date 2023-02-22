/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.svnee.easyevent.common.utils.reflect;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

public abstract class Invokable<T, R> implements AnnotatedElement, Member {

    private final AccessibleObject accessibleObject;
    private final Member member;

    <M extends AccessibleObject & Member> Invokable(M member) {

        checkNotNull(member);

        this.accessibleObject = member;
        this.member = member;
    }

    /** Returns {@link Invokable} of {@code method}. */
    public static Invokable<?, Object> from(Method method) {
        return new MethodInvokable<>(method);
    }

    /** Returns {@link Invokable} of {@code constructor}. */
    public static <T> Invokable<T, T> from(Constructor<T> constructor) {
        return new ConstructorInvokable<>(constructor);
    }

    @Override
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return accessibleObject.isAnnotationPresent(annotationClass);
    }

    @Override
    public final <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return accessibleObject.getAnnotation(annotationClass);
    }

    @Override
    public final Annotation[] getAnnotations() {
        return accessibleObject.getAnnotations();
    }

    @Override
    public final Annotation[] getDeclaredAnnotations() {
        return accessibleObject.getDeclaredAnnotations();
    }

    @Override
    public final String getName() {
        return member.getName();
    }

    @Override
    public final int getModifiers() {
        return member.getModifiers();
    }

    @Override
    public final boolean isSynthetic() {
        return member.isSynthetic();
    }

    /** Returns true if the element is public. */
    public final boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    /** Returns true if the element is private. */
    public final boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    /** Returns true if the element is static. */
    public final boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public final boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    /** Returns true if the method is abstract. */
    public final boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Invokable) {
            Invokable<?, ?> that = (Invokable<?, ?>) obj;
            return getOwnerType().equals(that.getOwnerType()) && member.equals(that.member);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return member.hashCode();
    }

    @Override
    public String toString() {
        return member.toString();
    }

    @SuppressWarnings("unchecked") // The declaring class is T's raw class, or one of its supertypes.
    @Override
    public final Class<? super T> getDeclaringClass() {
        return (Class<? super T>) member.getDeclaringClass();
    }

    /** Returns the type of {@code T}. */
    // Overridden in TypeToken#method() and TypeToken#constructor()
    @SuppressWarnings("unchecked") // The declaring class is T.
    public TypeToken<T> getOwnerType() {
        return (TypeToken<T>) TypeToken.of(getDeclaringClass());
    }

    abstract Type[] getGenericParameterTypes();

    /** This should never return a type that's not a subtype of Throwable. */
    abstract Type[] getGenericExceptionTypes();

    abstract Type getGenericReturnType();

    static class MethodInvokable<T> extends Invokable<T, Object> {

        final Method method;

        MethodInvokable(Method method) {
            super(method);
            this.method = method;
        }

        @Override
        Type getGenericReturnType() {
            return method.getGenericReturnType();
        }

        @Override
        Type[] getGenericParameterTypes() {
            return method.getGenericParameterTypes();
        }

        @Override
        Type[] getGenericExceptionTypes() {
            return method.getGenericExceptionTypes();
        }
    }

    static class ConstructorInvokable<T> extends Invokable<T, T> {

        final Constructor<?> constructor;

        ConstructorInvokable(Constructor<?> constructor) {
            super(constructor);
            this.constructor = constructor;
        }

        /**
         * If the class is parameterized, such as {@link java.util.ArrayList ArrayList}, this returns
         * {@code ArrayList<E>}.
         */
        @Override
        Type getGenericReturnType() {
            Class<?> declaringClass = getDeclaringClass();
            TypeVariable<?>[] typeParams = declaringClass.getTypeParameters();
            if (typeParams.length > 0) {
                return Types.newParameterizedType(declaringClass, typeParams);
            } else {
                return declaringClass;
            }
        }

        @Override
        Type[] getGenericParameterTypes() {
            Type[] types = constructor.getGenericParameterTypes();
            if (types.length > 0 && mayNeedHiddenThis()) {
                Class<?>[] rawParamTypes = constructor.getParameterTypes();
                if (types.length == rawParamTypes.length
                    && rawParamTypes[0] == getDeclaringClass().getEnclosingClass()) {
                    // first parameter is the hidden 'this'
                    return Arrays.copyOfRange(types, 1, types.length);
                }
            }
            return types;
        }

        @Override
        Type[] getGenericExceptionTypes() {
            return constructor.getGenericExceptionTypes();
        }

        private boolean mayNeedHiddenThis() {
            Class<?> declaringClass = constructor.getDeclaringClass();
            if (declaringClass.getEnclosingConstructor() != null) {
                // Enclosed in a constructor, needs hidden this
                return true;
            }
            Method enclosingMethod = declaringClass.getEnclosingMethod();
            if (enclosingMethod != null) {
                // Enclosed in a method, if it's not static, must need hidden this.
                return !Modifier.isStatic(enclosingMethod.getModifiers());
            } else {
                // Strictly, this doesn't necessarily indicate a hidden 'this' in the case of
                // static initializer. But there seems no way to tell in that case. :(
                // This may cause issues when an anonymous class is created inside a static initializer,
                // and the class's constructor's first parameter happens to be the enclosing class.
                // In such case, we may mistakenly think that the class is within a non-static context
                // and the first parameter is the hidden 'this'.
                return declaringClass.getEnclosingClass() != null
                    && !Modifier.isStatic(declaringClass.getModifiers());
            }
        }
    }
}
