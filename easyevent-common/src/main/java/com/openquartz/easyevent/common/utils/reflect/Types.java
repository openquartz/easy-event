/*
 * Copyright (C) 2011 The Guava Authors
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

package com.openquartz.easyevent.common.utils.reflect;

import static java.util.Objects.requireNonNull;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkArgument;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.common.constant.CommonConstants;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.Joiner;
import com.openquartz.easyevent.common.utils.MapUtils;

final class Types {

    static Type newArrayType(Type componentType) {
        if (componentType instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) componentType;
            Type[] lowerBounds = wildcard.getLowerBounds();
            checkArgument(lowerBounds.length <= 1);
            if (lowerBounds.length == 1) {
                return supertypeOf(newArrayType(lowerBounds[0]));
            } else {
                Type[] upperBounds = wildcard.getUpperBounds();
                checkArgument(upperBounds.length == 1);
                return subtypeOf(newArrayType(upperBounds[0]));
            }
        }
        return JavaVersion.CURRENT.newArrayType(componentType);
    }

    static ParameterizedType newParameterizedTypeWithOwner(Type ownerType, Class<?> rawType, Type... arguments) {
        if (ownerType == null) {
            return newParameterizedType(rawType, arguments);
        }
        // ParameterizedTypeImpl constructor already checks, but we want to throw NPE before IAE
        checkNotNull(arguments);
        checkArgument(rawType.getEnclosingClass() != null);
        return new ParameterizedTypeImpl(ownerType, rawType, arguments);
    }

    static ParameterizedType newParameterizedType(Class<?> rawType, Type... arguments) {
        return new ParameterizedTypeImpl(
            ClassOwnership.JVM_BEHAVIOR.getOwnerType(rawType), rawType, arguments);
    }

    private enum ClassOwnership {
        OWNED_BY_ENCLOSING_CLASS {
            @Override
            Class<?> getOwnerType(Class<?> rawType) {
                return rawType.getEnclosingClass();
            }
        },
        LOCAL_CLASS_HAS_NO_OWNER {
            @Override
            Class<?> getOwnerType(Class<?> rawType) {
                if (rawType.isLocalClass()) {
                    return null;
                } else {
                    return rawType.getEnclosingClass();
                }
            }
        };

        abstract Class<?> getOwnerType(Class<?> rawType);

        static final ClassOwnership JVM_BEHAVIOR = detectJvmBehavior();

        private static ClassOwnership detectJvmBehavior() {
            class LocalClass<T> {

            }
            Class<?> subclass = new LocalClass<String>() {
            }.getClass();
            // requireNonNull is safe because we're examining a type that's known to have a superclass.
            ParameterizedType parameterizedType =
                requireNonNull((ParameterizedType) subclass.getGenericSuperclass());
            for (ClassOwnership behavior : ClassOwnership.values()) {
                if (behavior.getOwnerType(LocalClass.class) == parameterizedType.getOwnerType()) {
                    return behavior;
                }
            }
            throw new AssertionError();
        }
    }

    static <D extends GenericDeclaration> TypeVariable<D> newArtificialTypeVariable(
        D declaration, String name, Type... bounds) {
        return newTypeVariableImpl(
            declaration, name, (bounds.length == 0) ? new Type[]{Object.class} : bounds);
    }

    static WildcardType subtypeOf(Type upperBound) {
        return new WildcardTypeImpl(new Type[0], new Type[]{upperBound});
    }

    static WildcardType supertypeOf(Type lowerBound) {
        return new WildcardTypeImpl(new Type[]{lowerBound}, new Type[]{Object.class});
    }

    static String toString(Type type) {
        return (type instanceof Class) ? ((Class<?>) type).getName() : type.toString();
    }

    static Type getComponentType(Type type) {
        checkNotNull(type);
        AtomicReference<Type> result = new AtomicReference<>();
        new TypeVisitor() {
            @Override
            void visitTypeVariable(TypeVariable<?> t) {
                result.set(subtypeOfComponentType(t.getBounds()));
            }

            @Override
            void visitWildcardType(WildcardType t) {
                result.set(subtypeOfComponentType(t.getUpperBounds()));
            }

            @Override
            void visitGenericArrayType(GenericArrayType t) {
                result.set(t.getGenericComponentType());
            }

            @Override
            void visitClass(Class<?> t) {
                result.set(t.getComponentType());
            }
        }.visit(type);
        return result.get();
    }

    private static Type subtypeOfComponentType(Type[] bounds) {
        for (Type bound : bounds) {
            Type componentType = getComponentType(bound);
            if (componentType != null) {
                // Only the first bound can be a class or array.
                // Bounds after the first can only be interfaces.
                if (componentType instanceof Class) {
                    Class<?> componentClass = (Class<?>) componentType;
                    if (componentClass.isPrimitive()) {
                        return componentClass;
                    }
                }
                return subtypeOf(componentType);
            }
        }
        return null;
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {

        private final Type componentType;

        GenericArrayTypeImpl(Type componentType) {
            this.componentType = JavaVersion.CURRENT.usedInGenericType(componentType);
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public String toString() {
            return Types.toString(componentType) + "[]";
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GenericArrayType) {
                GenericArrayType that = (GenericArrayType) obj;
                return Objects.equals(getGenericComponentType(), that.getGenericComponentType());
            }
            return false;
        }

        private static final long serialVersionUID = 0;
    }

    private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {

        private final Type ownerType;
        private final List<Type> argumentsList;
        private final Class<?> rawType;

        ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
            checkNotNull(rawType);
            checkArgument(typeArguments.length == rawType.getTypeParameters().length);
            disallowPrimitiveType(typeArguments, "type parameter");
            this.ownerType = ownerType;
            this.rawType = rawType;
            this.argumentsList = JavaVersion.CURRENT.usedInGenericType(typeArguments);
        }

        @Override
        public Type[] getActualTypeArguments() {
            return toArray(argumentsList);
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (ownerType != null && JavaVersion.CURRENT.jdkTypeDuplicatesOwnerName()) {
                builder.append(JavaVersion.CURRENT.typeName(ownerType)).append('.');
            }
            return builder
                .append(rawType.getName())
                .append('<')
                .append(Joiner.join(argumentsList, CommonConstants.COMMA))
                .append('>')
                .toString();
        }

        @Override
        public int hashCode() {
            return (ownerType == null ? 0 : ownerType.hashCode())
                ^ argumentsList.hashCode()
                ^ rawType.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ParameterizedType)) {
                return false;
            }
            ParameterizedType that = (ParameterizedType) other;
            return getRawType().equals(that.getRawType())
                && Objects.equals(getOwnerType(), that.getOwnerType())
                && Arrays.equals(getActualTypeArguments(), that.getActualTypeArguments());
        }

        private static final long serialVersionUID = 0;
    }

    private static <D extends GenericDeclaration> TypeVariable<D> newTypeVariableImpl(
        D genericDeclaration, String name, Type[] bounds) {
        TypeVariableImpl<D> typeVariableImpl = new TypeVariableImpl<>(genericDeclaration, name, bounds);
        @SuppressWarnings("unchecked")
        TypeVariable<D> typeVariable =
            Reflection.newProxy(
                TypeVariable.class, new TypeVariableInvocationHandler(typeVariableImpl));
        return typeVariable;
    }

    private static final class TypeVariableInvocationHandler implements InvocationHandler {

        private static final Map<String, Method> typeVariableMethods;

        static {
            Map<String, Method> builder = MapUtils.newHashMap();
            for (Method method : TypeVariableImpl.class.getMethods()) {
                if (method.getDeclaringClass().equals(TypeVariableImpl.class)) {
                    try {
                        method.setAccessible(true);
                    } catch (AccessControlException e) {
                        // OK: the method is accessible to us anyway. The setAccessible call is only for
                        // unusual execution environments where that might not be true.
                    }
                    builder.put(method.getName(), method);
                }
            }
            typeVariableMethods = builder;
        }

        private final TypeVariableImpl<?> typeVariableImpl;

        TypeVariableInvocationHandler(TypeVariableImpl<?> typeVariableImpl) {
            this.typeVariableImpl = typeVariableImpl;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            String methodName = method.getName();
            Method typeVariableMethod = typeVariableMethods.get(methodName);
            if (typeVariableMethod == null) {
                throw new UnsupportedOperationException(methodName);
            } else {
                try {
                    return typeVariableMethod.invoke(typeVariableImpl, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        }
    }

    private static final class TypeVariableImpl<D extends GenericDeclaration> {

        private final D genericDeclaration;
        private final String name;
        private final List<Type> bounds;

        TypeVariableImpl(D genericDeclaration, String name, Type[] bounds) {
            disallowPrimitiveType(bounds, "bound for type variable");

            checkNotNull(genericDeclaration);
            checkNotNull(name);

            this.genericDeclaration = genericDeclaration;
            this.name = name;
            this.bounds = CollectionUtils.newArrayList(bounds);
        }

        public D getGenericDeclaration() {
            return genericDeclaration;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            return genericDeclaration.hashCode() ^ name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (NativeTypeVariableEquals.NATIVE_TYPE_VARIABLE_ONLY) {
                // equal only to our TypeVariable implementation with identical bounds
                if (obj != null
                    && Proxy.isProxyClass(obj.getClass())
                    && Proxy.getInvocationHandler(obj) instanceof TypeVariableInvocationHandler) {
                    TypeVariableInvocationHandler typeVariableInvocationHandler =
                        (TypeVariableInvocationHandler) Proxy.getInvocationHandler(obj);
                    TypeVariableImpl<?> that = typeVariableInvocationHandler.typeVariableImpl;
                    return name.equals(that.getName())
                        && genericDeclaration.equals(that.getGenericDeclaration())
                        && bounds.equals(that.bounds);
                }
                return false;
            } else {
                // equal to any TypeVariable implementation regardless of bounds
                if (obj instanceof TypeVariable) {
                    TypeVariable<?> that = (TypeVariable<?>) obj;
                    return name.equals(that.getName())
                        && genericDeclaration.equals(that.getGenericDeclaration());
                }
                return false;
            }
        }
    }

    static final class WildcardTypeImpl implements WildcardType, Serializable {

        private final List<Type> lowerBounds;
        private final List<Type> upperBounds;

        WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
            disallowPrimitiveType(lowerBounds, "lower bound for wildcard");
            disallowPrimitiveType(upperBounds, "upper bound for wildcard");
            this.lowerBounds = JavaVersion.CURRENT.usedInGenericType(lowerBounds);
            this.upperBounds = JavaVersion.CURRENT.usedInGenericType(upperBounds);
        }

        @Override
        public Type[] getLowerBounds() {
            return toArray(lowerBounds);
        }

        @Override
        public Type[] getUpperBounds() {
            return toArray(upperBounds);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof WildcardType) {
                WildcardType that = (WildcardType) obj;
                return lowerBounds.equals(Arrays.asList(that.getLowerBounds()))
                    && upperBounds.equals(Arrays.asList(that.getUpperBounds()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return lowerBounds.hashCode() ^ upperBounds.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("?");
            for (Type lowerBound : lowerBounds) {
                builder.append(" super ").append(JavaVersion.CURRENT.typeName(lowerBound));
            }
            for (Type upperBound : upperBounds) {
                builder.append(" extends ").append(JavaVersion.CURRENT.typeName(upperBound));
            }
            return builder.toString();
        }

        private static final long serialVersionUID = 0;
    }

    private static Type[] toArray(Collection<Type> types) {
        return types.toArray(new Type[0]);
    }

    private static void disallowPrimitiveType(Type[] types, String usedAs) {
        for (Type type : types) {
            if (type instanceof Class) {
                Class<?> cls = (Class<?>) type;
                checkArgument(!cls.isPrimitive());
            }
        }
    }

    static Class<?> getArrayClass(Class<?> componentType) {

        return Array.newInstance(componentType, 0).getClass();
    }

    enum JavaVersion {
        JAVA6 {
            @Override
            GenericArrayType newArrayType(Type componentType) {
                return new GenericArrayTypeImpl(componentType);
            }

            @Override
            Type usedInGenericType(Type type) {
                checkNotNull(type);
                if (type instanceof Class) {
                    Class<?> cls = (Class<?>) type;
                    if (cls.isArray()) {
                        return new GenericArrayTypeImpl(cls.getComponentType());
                    }
                }
                return type;
            }
        },
        JAVA7 {
            @Override
            Type newArrayType(Type componentType) {
                if (componentType instanceof Class) {
                    return getArrayClass((Class<?>) componentType);
                } else {
                    return new GenericArrayTypeImpl(componentType);
                }
            }

            @Override
            Type usedInGenericType(Type type) {
                checkNotNull(type);
                return type;
            }
        },
        JAVA8 {
            @Override
            Type newArrayType(Type componentType) {
                return JAVA7.newArrayType(componentType);
            }

            @Override
            Type usedInGenericType(Type type) {
                return JAVA7.usedInGenericType(type);
            }

            @Override
            String typeName(Type type) {
                try {
                    Method getTypeName = Type.class.getMethod("getTypeName");
                    return (String) getTypeName.invoke(type);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError("Type.getTypeName should be available in Java 8");
                    /*
                     * Do not merge the 2 catch blocks below. javac would infer a type of
                     * ReflectiveOperationException, which Animal Sniffer would reject. (Old versions of
                     * Android don't *seem* to mind, but there might be edge cases of which we're unaware.)
                     */
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        JAVA9 {
            @Override
            Type newArrayType(Type componentType) {
                return JAVA8.newArrayType(componentType);
            }

            @Override
            Type usedInGenericType(Type type) {
                return JAVA8.usedInGenericType(type);
            }

            @Override
            String typeName(Type type) {
                return JAVA8.typeName(type);
            }

            @Override
            boolean jdkTypeDuplicatesOwnerName() {
                return false;
            }
        };

        static final JavaVersion CURRENT;

        static {
            if (AnnotatedElement.class.isAssignableFrom(TypeVariable.class)) {
                if (new TypeCapture<Entry<String, int[][]>>() {
                }.capture()
                    .toString()
                    .contains("java.util.Map.java.util.Map")) {
                    CURRENT = JAVA8;
                } else {
                    CURRENT = JAVA9;
                }
            } else if (new TypeCapture<int[]>() {
            }.capture() instanceof Class) {
                CURRENT = JAVA7;
            } else {
                CURRENT = JAVA6;
            }
        }

        abstract Type newArrayType(Type componentType);

        abstract Type usedInGenericType(Type type);

        final List<Type> usedInGenericType(Type[] types) {
            List<Type> builder = CollectionUtils.newArrayList();
            for (Type type : types) {
                builder.add(usedInGenericType(type));
            }
            return builder;
        }

        String typeName(Type type) {
            return Types.toString(type);
        }

        boolean jdkTypeDuplicatesOwnerName() {
            return true;
        }
    }

    static final class NativeTypeVariableEquals<X> {

        static final boolean NATIVE_TYPE_VARIABLE_ONLY =
            !NativeTypeVariableEquals.class.getTypeParameters()[0].equals(
                newArtificialTypeVariable(NativeTypeVariableEquals.class, "X"));
    }

    private Types() {
    }
}
