/*
 * Copyright (C) 2009 The Guava Authors
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

import static com.openquartz.easyevent.common.utils.ParamUtils.checkArgument;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.common.utils.ObjectUtils;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.Joiner;
import com.openquartz.easyevent.common.utils.MapUtils;

/**
 * TypeResolver
 *
 * @author svnee
 * @since 1.0.0
 */
public final class TypeResolver {

    private final TypeTable typeTable;

    public TypeResolver() {
        this.typeTable = new TypeTable();
    }

    private TypeResolver(TypeTable typeTable) {
        this.typeTable = typeTable;
    }

    static TypeResolver covariantly(Type contextType) {
        return new TypeResolver().where(TypeMappingIntrospector.getTypeMappings(contextType));
    }

    static TypeResolver invariantly(Type contextType) {
        Type invariantContext = WildcardCapturer.INSTANCE.capture(contextType);
        return new TypeResolver().where(TypeMappingIntrospector.getTypeMappings(invariantContext));
    }

    public TypeResolver where(Type formal, Type actual) {
        checkNotNull(formal);
        checkNotNull(actual);

        Map<TypeVariableKey, Type> mappings = MapUtils.newHashMap();

        populateTypeMappings(mappings, formal, actual);
        return where(mappings);
    }

    /** Returns a new {@code TypeResolver} with {@code variable} mapping to {@code type}. */
    TypeResolver where(Map<TypeVariableKey, ? extends Type> mappings) {
        return new TypeResolver(typeTable.where(mappings));
    }

    private static void populateTypeMappings(
        Map<TypeVariableKey, Type> mappings, Type from, Type to) {
        if (from.equals(to)) {
            return;
        }
        new TypeVisitor() {
            @Override
            void visitTypeVariable(TypeVariable<?> typeVariable) {
                mappings.put(new TypeVariableKey(typeVariable), to);
            }

            @Override
            void visitWildcardType(WildcardType fromWildcardType) {
                if (!(to instanceof WildcardType)) {
                    return; // okay to say <?> is anything
                }
                WildcardType toWildcardType = (WildcardType) to;
                Type[] fromUpperBounds = fromWildcardType.getUpperBounds();
                Type[] toUpperBounds = toWildcardType.getUpperBounds();
                Type[] fromLowerBounds = fromWildcardType.getLowerBounds();
                Type[] toLowerBounds = toWildcardType.getLowerBounds();
                checkArgument(fromUpperBounds.length == toUpperBounds.length
                    && fromLowerBounds.length == toLowerBounds.length);
                for (int i = 0; i < fromUpperBounds.length; i++) {
                    populateTypeMappings(mappings, fromUpperBounds[i], toUpperBounds[i]);
                }
                for (int i = 0; i < fromLowerBounds.length; i++) {
                    populateTypeMappings(mappings, fromLowerBounds[i], toLowerBounds[i]);
                }
            }

            @Override
            void visitParameterizedType(ParameterizedType fromParameterizedType) {
                if (to instanceof WildcardType) {
                    return; // Okay to say Foo<A> is <?>
                }
                ParameterizedType toParameterizedType = expectArgument(ParameterizedType.class, to);
                if (fromParameterizedType.getOwnerType() != null
                    && toParameterizedType.getOwnerType() != null) {
                    populateTypeMappings(
                        mappings, fromParameterizedType.getOwnerType(), toParameterizedType.getOwnerType());
                }
                checkArgument(fromParameterizedType.getRawType().equals(toParameterizedType.getRawType()));
                Type[] fromArgs = fromParameterizedType.getActualTypeArguments();
                Type[] toArgs = toParameterizedType.getActualTypeArguments();
                checkArgument(fromArgs.length == toArgs.length);
                for (int i = 0; i < fromArgs.length; i++) {
                    populateTypeMappings(mappings, fromArgs[i], toArgs[i]);
                }
            }

            @Override
            void visitGenericArrayType(GenericArrayType fromArrayType) {
                if (to instanceof WildcardType) {
                    return; // Okay to say A[] is <?>
                }
                Type componentType = Types.getComponentType(to);
                checkArgument(componentType != null);
                populateTypeMappings(mappings, fromArrayType.getGenericComponentType(), componentType);
            }

            @Override
            void visitClass(Class<?> fromClass) {
                if (to instanceof WildcardType) {
                    return; // Okay to say Foo is <?>
                }
                // Can't map from a raw class to anything other than itself or a wildcard.
                // You can't say "assuming String is Integer".
                // And we don't support "assuming String is T"; user has to say "assuming T is String".
                throw new IllegalArgumentException("No type mapping from " + fromClass + " to " + to);
            }
        }.visit(from);
    }

    /**
     * Resolves all type variables in {@code type} and all downstream types and returns a
     * corresponding type with type variables resolved.
     */
    public Type resolveType(Type type) {
        checkNotNull(type);
        if (type instanceof TypeVariable) {
            return typeTable.resolve((TypeVariable<?>) type);
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type);
        } else if (type instanceof WildcardType) {
            return resolveWildcardType((WildcardType) type);
        } else {
            // if Class<?>, no resolution needed, we are done.
            return type;
        }
    }

    Type[] resolveTypesInPlace(Type[] types) {
        for (int i = 0; i < types.length; i++) {
            types[i] = resolveType(types[i]);
        }
        return types;
    }

    private Type[] resolveTypes(Type[] types) {
        Type[] result = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = resolveType(types[i]);
        }
        return result;
    }

    private WildcardType resolveWildcardType(WildcardType type) {
        Type[] lowerBounds = type.getLowerBounds();
        Type[] upperBounds = type.getUpperBounds();
        return new Types.WildcardTypeImpl(resolveTypes(lowerBounds), resolveTypes(upperBounds));
    }

    private Type resolveGenericArrayType(GenericArrayType type) {
        Type componentType = type.getGenericComponentType();
        Type resolvedComponentType = resolveType(componentType);
        return Types.newArrayType(resolvedComponentType);
    }

    private ParameterizedType resolveParameterizedType(ParameterizedType type) {
        Type owner = type.getOwnerType();
        Type resolvedOwner = (owner == null) ? null : resolveType(owner);
        Type resolvedRawType = resolveType(type.getRawType());

        Type[] args = type.getActualTypeArguments();
        Type[] resolvedArgs = resolveTypes(args);
        return Types.newParameterizedTypeWithOwner(
            resolvedOwner, (Class<?>) resolvedRawType, resolvedArgs);
    }

    private static <T> T expectArgument(Class<T> type, Object arg) {
        try {
            return type.cast(arg);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(arg + " is not a " + type.getSimpleName());
        }
    }

    /** A TypeTable maintains mapping from {@link TypeVariable} to types. */
    private static class TypeTable {

        private final Map<TypeVariableKey, Type> map;

        TypeTable() {
            this.map = MapUtils.newHashMap();
        }

        private TypeTable(Map<TypeVariableKey, Type> map) {
            this.map = map;
        }

        /** Returns a new {@code TypeResolver} with {@code variable} mapping to {@code type}. */
        final TypeTable where(Map<TypeVariableKey, ? extends Type> mappings) {
            Map<TypeVariableKey, Type> builder = MapUtils.newHashMap();
            builder.putAll(map);
            for (Entry<TypeVariableKey, ? extends Type> mapping : mappings.entrySet()) {
                TypeVariableKey variable = mapping.getKey();
                Type type = mapping.getValue();
                checkArgument(!variable.equalsType(type));
                builder.put(variable, type);
            }
            return new TypeTable(builder);
        }

        final Type resolve(TypeVariable<?> var) {
            TypeTable unguarded = this;
            TypeTable guarded =
                new TypeTable() {
                    @Override
                    public Type resolveInternal(TypeVariable<?> intermediateVar, TypeTable forDependent) {
                        if (intermediateVar.getGenericDeclaration().equals(var.getGenericDeclaration())) {
                            return intermediateVar;
                        }
                        return unguarded.resolveInternal(intermediateVar, forDependent);
                    }
                };
            return resolveInternal(var, guarded);
        }

        /**
         * Resolves {@code var} using the encapsulated type mapping. If it maps to yet another
         * non-reified type or has bounds, {@code forDependants} is used to do further resolution, which
         * doesn't try to resolve any type variable on generic declarations that are already being
         * resolved.
         *
         * <p>Should only be called and overridden by {@link #resolve(TypeVariable)}.
         */
        Type resolveInternal(TypeVariable<?> var, TypeTable forDependants) {
            Type type = map.get(new TypeVariableKey(var));
            if (type == null) {
                Type[] bounds = var.getBounds();
                if (bounds.length == 0) {
                    return var;
                }
                Type[] resolvedBounds = new TypeResolver(forDependants).resolveTypes(bounds);
                /*
                 * We'd like to simply create our own TypeVariable with the newly resolved bounds. There's
                 * just one problem: Starting with JDK 7u51, the JDK TypeVariable's equals() method doesn't
                 * recognize instances of our TypeVariable implementation. This is a problem because users
                 * compare TypeVariables from the JDK against TypeVariables returned by TypeResolver. To
                 * work with all JDK versions, TypeResolver must return the appropriate TypeVariable
                 * implementation in each of the three possible cases:
                 *
                 * 1. Prior to JDK 7u51, the JDK TypeVariable implementation interoperates with ours.
                 * Therefore, we can always create our own TypeVariable.
                 *
                 * 2. Starting with JDK 7u51, the JDK TypeVariable implementations does not interoperate
                 * with ours. Therefore, we have to be careful about whether we create our own TypeVariable:
                 *
                 * 2a. If the resolved types are identical to the original types, then we can return the
                 * original, identical JDK TypeVariable. By doing so, we sidestep the problem entirely.
                 *
                 * 2b. If the resolved types are different from the original types, things are trickier. The
                 * only way to get a TypeVariable instance for the resolved types is to create our own. The
                 * created TypeVariable will not interoperate with any JDK TypeVariable. But this is OK: We
                 * don't _want_ our new TypeVariable to be equal to the JDK TypeVariable because it has
                 * _different bounds_ than the JDK TypeVariable. And it wouldn't make sense for our new
                 * TypeVariable to be equal to any _other_ JDK TypeVariable, either, because any other JDK
                 * TypeVariable must have a different declaration or name. The only TypeVariable that our
                 * new TypeVariable _will_ be equal to is an equivalent TypeVariable that was also created
                 * by us. And that equality is guaranteed to hold because it doesn't involve the JDK
                 * TypeVariable implementation at all.
                 */
                if (Types.NativeTypeVariableEquals.NATIVE_TYPE_VARIABLE_ONLY
                    && Arrays.equals(bounds, resolvedBounds)) {
                    return var;
                }
                return Types.newArtificialTypeVariable(
                    var.getGenericDeclaration(), var.getName(), resolvedBounds);
            }
            // in case the type is yet another type variable.
            return new TypeResolver(forDependants).resolveType(type);
        }
    }

    private static final class TypeMappingIntrospector extends TypeVisitor {

        private final Map<TypeVariableKey, Type> mappings = MapUtils.newHashMap();

        /**
         * Returns type mappings using type parameters and type arguments found in the generic
         * superclass and the super interfaces of {@code contextClass}.
         */
        static Map<TypeVariableKey, Type> getTypeMappings(Type contextType) {
            checkNotNull(contextType);
            TypeMappingIntrospector introspector = new TypeMappingIntrospector();
            introspector.visit(contextType);
            return MapUtils.copyOf(introspector.mappings);
        }

        @Override
        void visitClass(Class<?> clazz) {
            visit(clazz.getGenericSuperclass());
            visit(clazz.getGenericInterfaces());
        }

        @Override
        void visitParameterizedType(ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            TypeVariable<?>[] vars = rawClass.getTypeParameters();
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            checkArgument(vars.length == typeArgs.length);
            for (int i = 0; i < vars.length; i++) {
                map(new TypeVariableKey(vars[i]), typeArgs[i]);
            }
            visit(rawClass);
            visit(parameterizedType.getOwnerType());
        }

        @Override
        void visitTypeVariable(TypeVariable<?> t) {
            visit(t.getBounds());
        }

        @Override
        void visitWildcardType(WildcardType t) {
            visit(t.getUpperBounds());
        }

        private void map(TypeVariableKey var, Type arg) {
            if (mappings.containsKey(var)) {
                // Mapping already established
                // This is possible when following both superClass -> enclosingClass
                // and enclosingclass -> superClass paths.
                // Since we follow the path of superclass first, enclosing second,
                // superclass mapping should take precedence.
                return;
            }
            // First, check whether var -> arg forms a cycle
            for (Type t = arg; t != null; t = mappings.get(TypeVariableKey.forLookup(t))) {
                if (var.equalsType(t)) {
                    // cycle detected, remove the entire cycle from the mapping so that
                    // each type variable resolves deterministically to itself.
                    // Otherwise, a F -> T cycle will end up resolving both F and T
                    // nondeterministically to either F or T.
                    for (Type x = arg; x != null; x = mappings.remove(TypeVariableKey.forLookup(x))) {
                    }
                    return;
                }
            }
            mappings.put(var, arg);
        }
    }

    private static class WildcardCapturer {

        static final WildcardCapturer INSTANCE = new WildcardCapturer();

        private final AtomicInteger id;

        private WildcardCapturer() {
            this(new AtomicInteger());
        }

        private WildcardCapturer(AtomicInteger id) {
            this.id = id;
        }

        final Type capture(Type type) {
            checkNotNull(type);
            if (type instanceof Class) {
                return type;
            }
            if (type instanceof TypeVariable) {
                return type;
            }
            if (type instanceof GenericArrayType) {
                GenericArrayType arrayType = (GenericArrayType) type;
                return Types.newArrayType(
                    notForTypeVariable().capture(arrayType.getGenericComponentType()));
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                TypeVariable<?>[] typeVars = rawType.getTypeParameters();
                Type[] typeArgs = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < typeArgs.length; i++) {
                    typeArgs[i] = forTypeVariable(typeVars[i]).capture(typeArgs[i]);
                }
                return Types.newParameterizedTypeWithOwner(
                    notForTypeVariable().captureNullable(parameterizedType.getOwnerType()),
                    rawType,
                    typeArgs);
            }
            if (type instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) type;
                Type[] lowerBounds = wildcardType.getLowerBounds();
                if (lowerBounds.length == 0) { // ? extends something changes to capture-of
                    return captureAsTypeVariable(wildcardType.getUpperBounds());
                } else {
                    return type;
                }
            }
            throw new AssertionError("must have been one of the known types");
        }

        TypeVariable<?> captureAsTypeVariable(Type[] upperBounds) {
            String name =
                "capture#" + id.incrementAndGet() + "-of ? extends " + Joiner.join("&", upperBounds);
            return Types.newArtificialTypeVariable(WildcardCapturer.class, name, upperBounds);
        }

        private WildcardCapturer forTypeVariable(TypeVariable<?> typeParam) {
            return new WildcardCapturer(id) {
                @Override
                TypeVariable<?> captureAsTypeVariable(Type[] upperBounds) {
                    Set<Type> combined = new LinkedHashSet<>(CollectionUtils.newArrayList(upperBounds));
                    // Since this is an artificially generated type variable, we don't bother checking
                    // subtyping between declared type bound and actual type bound. So it's possible that we
                    // may generate something like <capture#1-of ? extends Foo&SubFoo>.
                    // Checking subtype between declared and actual type bounds
                    // adds recursive isSubtypeOf() call and feels complicated.
                    // There is no contract one way or another as long as isSubtypeOf() works as expected.
                    combined.addAll(CollectionUtils.newArrayList(typeParam.getBounds()));
                    if (combined.size() > 1) { // Object is implicit and only useful if it's the only bound.
                        combined.remove(Object.class);
                    }
                    return super.captureAsTypeVariable(combined.toArray(new Type[0]));
                }
            };
        }

        private WildcardCapturer notForTypeVariable() {
            return new WildcardCapturer(id);
        }

        private Type captureNullable(Type type) {
            if (type == null) {
                return null;
            }
            return capture(type);
        }
    }

    static final class TypeVariableKey {

        private final TypeVariable<?> var;

        TypeVariableKey(TypeVariable<?> var) {
            checkNotNull(var);
            this.var = var;
        }

        @Override
        public int hashCode() {
            return ObjectUtils.hashCode(var.getGenericDeclaration(), var.getName());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeVariableKey) {
                TypeVariableKey that = (TypeVariableKey) obj;
                return equalsTypeVariable(that.var);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return var.toString();
        }

        /** Wraps {@code t} in a {@code TypeVariableKey} if it's a type variable. */
        static TypeVariableKey forLookup(Type t) {
            if (t instanceof TypeVariable) {
                return new TypeVariableKey((TypeVariable<?>) t);
            } else {
                return null;
            }
        }

        /**
         * Returns true if {@code type} is a {@code TypeVariable} with the same name and declared by the
         * same {@code GenericDeclaration}.
         */
        boolean equalsType(Type type) {
            if (type instanceof TypeVariable) {
                return equalsTypeVariable((TypeVariable<?>) type);
            } else {
                return false;
            }
        }

        private boolean equalsTypeVariable(TypeVariable<?> that) {
            return var.getGenericDeclaration().equals(that.getGenericDeclaration())
                && var.getName().equals(that.getName());
        }
    }
}
