/*
 * Copyright (C) 2006 The Guava Authors
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

import static java.util.Objects.requireNonNull;
import static org.svnee.easyevent.common.utils.ParamUtils.checkArgument;
import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.common.utils.MapUtils;

public abstract class TypeToken<T> extends TypeCapture<T> implements Serializable {

    private final Type runtimeType;

    private transient TypeResolver invariantTypeResolver;

    private transient TypeResolver covariantTypeResolver;

    private TypeToken(Type type) {

        checkNotNull(type);

        this.runtimeType = type;
    }

    public static <T> TypeToken<T> of(Class<T> type) {
        return new SimpleTypeToken<>(type);
    }

    public static TypeToken<?> of(Type type) {
        return new SimpleTypeToken<>(type);
    }

    public final Class<? super T> getRawType() {
        // For wildcard or type variable, the first bound determines the runtime type.
        Class<?> rawType = getRawTypes().iterator().next();
        @SuppressWarnings("unchecked") // raw type is |T|
        Class<? super T> result = (Class<? super T>) rawType;
        return result;
    }

    public final Type getType() {
        return runtimeType;
    }

    private TypeToken<?> resolveSupertype(Type type) {
        TypeToken<?> supertype = of(getCovariantTypeResolver().resolveType(type));
        // super types' type mapping is a subset of type mapping of this type.
        supertype.covariantTypeResolver = covariantTypeResolver;
        supertype.invariantTypeResolver = invariantTypeResolver;
        return supertype;
    }

    final TypeToken<? super T> getGenericSuperclass() {
        if (runtimeType instanceof TypeVariable) {
            // First bound is always the super class, if one exists.
            return boundAsSuperclass(((TypeVariable<?>) runtimeType).getBounds()[0]);
        }
        if (runtimeType instanceof WildcardType) {
            // wildcard has one and only one upper bound.
            return boundAsSuperclass(((WildcardType) runtimeType).getUpperBounds()[0]);
        }
        Type superclass = getRawType().getGenericSuperclass();
        if (superclass == null) {
            return null;
        }
        @SuppressWarnings("unchecked") // super class of T
        TypeToken<? super T> superToken = (TypeToken<? super T>) resolveSupertype(superclass);
        return superToken;
    }

    private TypeToken<? super T> boundAsSuperclass(Type bound) {
        TypeToken<?> token = of(bound);
        if (token.getRawType().isInterface()) {
            return null;
        }
        @SuppressWarnings("unchecked") // only upper bound of T is passed in.
        TypeToken<? super T> superclass = (TypeToken<? super T>) token;
        return superclass;
    }

    final List<TypeToken<? super T>> getGenericInterfaces() {
        if (runtimeType instanceof TypeVariable) {
            return boundsAsInterfaces(((TypeVariable<?>) runtimeType).getBounds());
        }
        if (runtimeType instanceof WildcardType) {
            return boundsAsInterfaces(((WildcardType) runtimeType).getUpperBounds());
        }
        List<TypeToken<? super T>> builder = CollectionUtils.newArrayList();
        for (Type interfaceType : getRawType().getGenericInterfaces()) {
            @SuppressWarnings("unchecked") // interface of T
            TypeToken<? super T> resolvedInterface =
                (TypeToken<? super T>) resolveSupertype(interfaceType);
            builder.add(resolvedInterface);
        }
        return builder;
    }

    private List<TypeToken<? super T>> boundsAsInterfaces(Type[] bounds) {
        List<TypeToken<? super T>> builder = CollectionUtils.newArrayList();
        for (Type bound : bounds) {
            @SuppressWarnings("unchecked") // upper bound of T
            TypeToken<? super T> boundType = (TypeToken<? super T>) of(bound);
            if (boundType.getRawType().isInterface()) {
                builder.add(boundType);
            }
        }
        return builder;
    }

    public final TypeSet getTypes() {
        return new TypeSet();
    }

    public final TypeToken<? super T> getSupertype(Class<? super T> superclass) {
        checkArgument(this.someRawTypeIsSubclassOf(superclass));
        if (runtimeType instanceof TypeVariable) {
            return getSupertypeFromUpperBounds(superclass, ((TypeVariable<?>) runtimeType).getBounds());
        }
        if (runtimeType instanceof WildcardType) {
            return getSupertypeFromUpperBounds(superclass, ((WildcardType) runtimeType).getUpperBounds());
        }
        if (superclass.isArray()) {
            return getArraySupertype(superclass);
        }
        @SuppressWarnings("unchecked") // resolved supertype
        TypeToken<? super T> supertype =
            (TypeToken<? super T>) resolveSupertype(toGenericType(superclass).runtimeType);
        return supertype;
    }

    public final TypeToken<? extends T> getSubtype(Class<?> subclass) {
        checkArgument(!(runtimeType instanceof TypeVariable));
        if (runtimeType instanceof WildcardType) {
            return getSubtypeFromLowerBounds(subclass, ((WildcardType) runtimeType).getLowerBounds());
        }
        // unwrap array type if necessary
        if (isArray()) {
            return getArraySubtype(subclass);
        }
        // At this point, it's either a raw class or parameterized type.
        checkArgument(getRawType().isAssignableFrom(subclass));
        Type resolvedTypeArgs = resolveTypeArgsForSubclass(subclass);
        @SuppressWarnings("unchecked") // guarded by the isAssignableFrom() statement above
        TypeToken<? extends T> subtype = (TypeToken<? extends T>) of(resolvedTypeArgs);
        checkArgument(subtype.isSubtypeOf(this));
        return subtype;
    }

    public final boolean isSubtypeOf(TypeToken<?> type) {
        return isSubtypeOf(type.getType());
    }

    public final boolean isSubtypeOf(Type supertype) {
        checkNotNull(supertype);
        if (supertype instanceof WildcardType) {
            // if 'supertype' is <? super Foo>, 'this' can be:
            // Foo, SubFoo, <? extends Foo>.
            // if 'supertype' is <? extends Foo>, nothing is a subtype.
            return any(((WildcardType) supertype).getLowerBounds()).isSupertypeOf(runtimeType);
        }
        // if 'this' is wildcard, it's a suptype of to 'supertype' if any of its "extends"
        // bounds is a subtype of 'supertype'.
        if (runtimeType instanceof WildcardType) {
            // <? super Base> is of no use in checking 'from' being a subtype of 'to'.
            return any(((WildcardType) runtimeType).getUpperBounds()).isSubtypeOf(supertype);
        }
        // if 'this' is type variable, it's a subtype if any of its "extends"
        // bounds is a subtype of 'supertype'.
        if (runtimeType instanceof TypeVariable) {
            return runtimeType.equals(supertype)
                || any(((TypeVariable<?>) runtimeType).getBounds()).isSubtypeOf(supertype);
        }
        if (runtimeType instanceof GenericArrayType) {
            return of(supertype).isSupertypeOfArray((GenericArrayType) runtimeType);
        }
        // Proceed to regular Type subtype check
        if (supertype instanceof Class) {
            return this.someRawTypeIsSubclassOf((Class<?>) supertype);
        } else if (supertype instanceof ParameterizedType) {
            return this.isSubtypeOfParameterizedType((ParameterizedType) supertype);
        } else if (supertype instanceof GenericArrayType) {
            return this.isSubtypeOfArrayType((GenericArrayType) supertype);
        } else { // to instanceof TypeVariable
            return false;
        }
    }

    public final boolean isArray() {
        return getComponentType() != null;
    }

    public final TypeToken<?> getComponentType() {
        Type componentType = Types.getComponentType(runtimeType);
        if (componentType == null) {
            return null;
        }
        return of(componentType);
    }

    public final Invokable<T, Object> method(Method method) {
        checkArgument(this.someRawTypeIsSubclassOf(method.getDeclaringClass()));
        return new Invokable.MethodInvokable<T>(method) {
            @Override
            Type getGenericReturnType() {
                return getCovariantTypeResolver().resolveType(super.getGenericReturnType());
            }

            @Override
            Type[] getGenericParameterTypes() {
                return getInvariantTypeResolver().resolveTypesInPlace(super.getGenericParameterTypes());
            }

            @Override
            Type[] getGenericExceptionTypes() {
                return getCovariantTypeResolver().resolveTypesInPlace(super.getGenericExceptionTypes());
            }

            @Override
            public TypeToken<T> getOwnerType() {
                return TypeToken.this;
            }

            @Override
            public String toString() {
                return getOwnerType() + "." + super.toString();
            }
        };
    }

    public class TypeSet extends ForwardingSet<TypeToken<? super T>> implements Serializable {

        private transient Set<TypeToken<? super T>> types;

        TypeSet() {
        }

        public TypeSet interfaces() {
            return new InterfaceSet(this);
        }

        public TypeSet classes() {
            return new ClassSet();
        }

        @Override
        protected Set<TypeToken<? super T>> delegate() {
            Set<TypeToken<? super T>> filteredTypes = types;
            if (filteredTypes == null) {
                // Java has no way to express ? super T when we parameterize TypeToken vs. Class.
                @SuppressWarnings({"rawtypes", "unchecked"})
                List<TypeToken<? super T>> collectedTypes = (List) TypeCollector.FOR_GENERIC_TYPE
                    .collectTypes(TypeToken.this);
                return (types = collectedTypes.stream()
                    .filter(TypeFilter.IGNORE_TYPE_VARIABLE_OR_WILDCARD)
                    .collect(Collectors.toSet()));
            } else {
                return filteredTypes;
            }
        }

        public Set<Class<? super T>> rawTypes() {
            // Java has no way to express ? super T when we parameterize TypeToken vs. Class.
            @SuppressWarnings({"unchecked", "rawtypes"})
            List<Class<? super T>> collectedTypes = (List) TypeCollector.FOR_RAW_TYPE
                .collectTypes(getRawTypes());
            return CollectionUtils.copyToSetOf(collectedTypes);
        }

        private static final long serialVersionUID = 0;
    }

    private final class InterfaceSet extends TypeSet {

        private final transient TypeSet allTypes;
        private transient Set<TypeToken<? super T>> interfaces;

        InterfaceSet(TypeSet allTypes) {
            this.allTypes = allTypes;
        }

        @Override
        protected Set<TypeToken<? super T>> delegate() {
            Set<TypeToken<? super T>> result = interfaces;
            if (result == null) {
                return (interfaces =
                    allTypes.stream().filter(TypeFilter.INTERFACE_ONLY).collect(Collectors.toSet()));
            } else {
                return result;
            }
        }

        @Override
        public TypeSet interfaces() {
            return this;
        }

        @Override
        public Set<Class<? super T>> rawTypes() {
            // Java has no way to express ? super T when we parameterize TypeToken vs. Class.
            @SuppressWarnings({"unchecked", "rawtypes"})
            List<Class<? super T>> collectedTypes =
                (List) TypeCollector.FOR_RAW_TYPE.collectTypes(getRawTypes());
            return collectedTypes.stream().filter(Class::isInterface).collect(Collectors.toSet());
        }

        @Override
        public TypeSet classes() {
            throw new UnsupportedOperationException("interfaces().classes() not supported.");
        }

        private Object readResolve() {
            return getTypes().interfaces();
        }

        private static final long serialVersionUID = 0;
    }

    private final class ClassSet extends TypeSet {

        private transient Set<TypeToken<? super T>> classes;

        @Override
        protected Set<TypeToken<? super T>> delegate() {
            Set<TypeToken<? super T>> result = classes;
            if (result == null) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                List<TypeToken<? super T>> collectedTypes =
                    (List) TypeCollector.FOR_GENERIC_TYPE.classesOnly()
                        .collectTypes(TypeToken.this);
                return (classes =
                    collectedTypes.stream()
                        .filter(TypeFilter.IGNORE_TYPE_VARIABLE_OR_WILDCARD)
                        .collect(Collectors.toSet()));
            } else {
                return result;
            }
        }

        @Override
        public TypeSet classes() {
            return this;
        }

        @Override
        public Set<Class<? super T>> rawTypes() {
            List<Class<? super T>> collectedTypes = (List) TypeCollector.FOR_RAW_TYPE.classesOnly()
                .collectTypes(getRawTypes());
            return CollectionUtils.copyToSetOf(collectedTypes);
        }

        @Override
        public TypeSet interfaces() {
            throw new UnsupportedOperationException("classes().interfaces() not supported.");
        }

        private Object readResolve() {
            return getTypes().classes();
        }

        private static final long serialVersionUID = 0;
    }

    private enum TypeFilter implements Predicate<TypeToken<?>> {
        IGNORE_TYPE_VARIABLE_OR_WILDCARD {
            @Override
            public boolean test(TypeToken<?> type) {
                return !(type.runtimeType instanceof TypeVariable
                    || type.runtimeType instanceof WildcardType);
            }
        },
        INTERFACE_ONLY {
            @Override
            public boolean test(TypeToken<?> type) {
                return type.getRawType().isInterface();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TypeToken) {
            TypeToken<?> that = (TypeToken<?>) o;
            return runtimeType.equals(that.runtimeType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return runtimeType.hashCode();
    }

    @Override
    public String toString() {
        return Types.toString(runtimeType);
    }

    protected Object writeReplace() {
        // TypeResolver just transforms the type to our own impls that are Serializable
        // except TypeVariable.
        return of(new TypeResolver().resolveType(runtimeType));
    }

    private boolean someRawTypeIsSubclassOf(Class<?> superclass) {
        for (Class<?> rawType : getRawTypes()) {
            if (superclass.isAssignableFrom(rawType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSubtypeOfParameterizedType(ParameterizedType supertype) {
        Class<?> matchedClass = of(supertype).getRawType();
        if (!someRawTypeIsSubclassOf(matchedClass)) {
            return false;
        }
        TypeVariable<?>[] typeVars = matchedClass.getTypeParameters();
        Type[] supertypeArgs = supertype.getActualTypeArguments();
        for (int i = 0; i < typeVars.length; i++) {
            Type subtypeParam = getCovariantTypeResolver().resolveType(typeVars[i]);
            // If 'supertype' is "List<? extends CharSequence>"
            // and 'this' is StringArrayList,
            // First step is to figure out StringArrayList "is-a" List<E> where <E> = String.
            // String is then matched against <? extends CharSequence>, the supertypeArgs[0].
            if (!of(subtypeParam).is(supertypeArgs[i], typeVars[i])) {
                return false;
            }
        }
        // We only care about the case when the supertype is a non-static inner class
        // in which case we need to make sure the subclass's owner type is a subtype of the
        // supertype's owner.
        return Modifier.isStatic(((Class<?>) supertype.getRawType()).getModifiers())
            || supertype.getOwnerType() == null
            || isOwnedBySubtypeOf(supertype.getOwnerType());
    }

    private boolean isSubtypeOfArrayType(GenericArrayType supertype) {
        if (runtimeType instanceof Class) {
            Class<?> fromClass = (Class<?>) runtimeType;
            if (!fromClass.isArray()) {
                return false;
            }
            return of(fromClass.getComponentType()).isSubtypeOf(supertype.getGenericComponentType());
        } else if (runtimeType instanceof GenericArrayType) {
            GenericArrayType fromArrayType = (GenericArrayType) runtimeType;
            return of(fromArrayType.getGenericComponentType())
                .isSubtypeOf(supertype.getGenericComponentType());
        } else {
            return false;
        }
    }

    private boolean isSupertypeOfArray(GenericArrayType subtype) {
        if (runtimeType instanceof Class) {
            Class<?> thisClass = (Class<?>) runtimeType;
            if (!thisClass.isArray()) {
                return thisClass.isAssignableFrom(Object[].class);
            }
            return of(subtype.getGenericComponentType()).isSubtypeOf(thisClass.getComponentType());
        } else if (runtimeType instanceof GenericArrayType) {
            return of(subtype.getGenericComponentType())
                .isSubtypeOf(((GenericArrayType) runtimeType).getGenericComponentType());
        } else {
            return false;
        }
    }

    private boolean is(Type formalType, TypeVariable<?> declaration) {
        if (runtimeType.equals(formalType)) {
            return true;
        }
        if (formalType instanceof WildcardType) {
            WildcardType your = canonicalizeWildcardType(declaration, (WildcardType) formalType);
            // if "formalType" is <? extends Foo>, "this" can be:
            // Foo, SubFoo, <? extends Foo>, <? extends SubFoo>, <T extends Foo> or
            // <T extends SubFoo>.
            // if "formalType" is <? super Foo>, "this" can be:
            // Foo, SuperFoo, <? super Foo> or <? super SuperFoo>.
            return every(your.getUpperBounds()).isSupertypeOf(runtimeType)
                && every(your.getLowerBounds()).isSubtypeOf(runtimeType);
        }
        return canonicalizeWildcardsInType(runtimeType).equals(canonicalizeWildcardsInType(formalType));
    }

    private static Type canonicalizeTypeArg(TypeVariable<?> declaration, Type typeArg) {
        return typeArg instanceof WildcardType
            ? canonicalizeWildcardType(declaration, ((WildcardType) typeArg))
            : canonicalizeWildcardsInType(typeArg);
    }

    private static Type canonicalizeWildcardsInType(Type type) {
        if (type instanceof ParameterizedType) {
            return canonicalizeWildcardsInParameterizedType((ParameterizedType) type);
        }
        if (type instanceof GenericArrayType) {
            return Types.newArrayType(
                canonicalizeWildcardsInType(((GenericArrayType) type).getGenericComponentType()));
        }
        return type;
    }

    // WARNING: the returned type may have empty upper bounds, which may violate common expectations
    // by user code or even some of our own code. It's fine for the purpose of checking subtypes.
    // Just don't ever let the user access it.
    private static WildcardType canonicalizeWildcardType(
        TypeVariable<?> declaration, WildcardType type) {
        Type[] declared = declaration.getBounds();
        List<Type> upperBounds = new ArrayList<>();
        for (Type bound : type.getUpperBounds()) {
            if (!any(declared).isSubtypeOf(bound)) {
                upperBounds.add(canonicalizeWildcardsInType(bound));
            }
        }
        return new Types.WildcardTypeImpl(type.getLowerBounds(), upperBounds.toArray(new Type[0]));
    }

    private static ParameterizedType canonicalizeWildcardsInParameterizedType(
        ParameterizedType type) {
        Class<?> rawType = (Class<?>) type.getRawType();
        TypeVariable<?>[] typeVars = rawType.getTypeParameters();
        Type[] typeArgs = type.getActualTypeArguments();
        for (int i = 0; i < typeArgs.length; i++) {
            typeArgs[i] = canonicalizeTypeArg(typeVars[i], typeArgs[i]);
        }
        return Types.newParameterizedTypeWithOwner(type.getOwnerType(), rawType, typeArgs);
    }

    private static Bounds every(Type[] bounds) {
        // Every bound must match. On any false, result is false.
        return new Bounds(bounds, false);
    }

    private static Bounds any(Type[] bounds) {
        // Any bound matches. On any true, result is true.
        return new Bounds(bounds, true);
    }

    private static class Bounds {

        private final Type[] bounds;
        private final boolean target;

        Bounds(Type[] bounds, boolean target) {
            this.bounds = bounds;
            this.target = target;
        }

        boolean isSubtypeOf(Type supertype) {
            for (Type bound : bounds) {
                if (of(bound).isSubtypeOf(supertype) == target) {
                    return target;
                }
            }
            return !target;
        }

        boolean isSupertypeOf(Type subtype) {
            TypeToken<?> type = of(subtype);
            for (Type bound : bounds) {
                if (type.isSubtypeOf(bound) == target) {
                    return target;
                }
            }
            return !target;
        }
    }

    private Set<Class<? super T>> getRawTypes() {
        Set<Class<?>> builder = CollectionUtils.newHashSet();
        new TypeVisitor() {
            @Override
            void visitTypeVariable(TypeVariable<?> t) {
                visit(t.getBounds());
            }

            @Override
            void visitWildcardType(WildcardType t) {
                visit(t.getUpperBounds());
            }

            @Override
            void visitParameterizedType(ParameterizedType t) {
                builder.add((Class<?>) t.getRawType());
            }

            @Override
            void visitClass(Class<?> t) {
                builder.add(t);
            }

            @Override
            void visitGenericArrayType(GenericArrayType t) {
                builder.add(Types.getArrayClass(of(t.getGenericComponentType()).getRawType()));
            }
        }.visit(runtimeType);
        // Cast from ImmutableSet<Class<?>> to ImmutableSet<Class<? super T>>
        return (Set) builder;
    }

    private boolean isOwnedBySubtypeOf(Type supertype) {
        for (TypeToken<?> type : getTypes()) {
            Type ownerType = type.getOwnerTypeIfPresent();
            if (ownerType != null && of(ownerType).isSubtypeOf(supertype)) {
                return true;
            }
        }
        return false;
    }

    private Type getOwnerTypeIfPresent() {
        if (runtimeType instanceof ParameterizedType) {
            return ((ParameterizedType) runtimeType).getOwnerType();
        } else if (runtimeType instanceof Class<?>) {
            return ((Class<?>) runtimeType).getEnclosingClass();
        } else {
            return null;
        }
    }

    static <T> TypeToken<? extends T> toGenericType(Class<T> cls) {
        if (cls.isArray()) {
            Type arrayOfGenericType =
                Types.newArrayType(
                    // If we are passed with int[].class, don't turn it to GenericArrayType
                    toGenericType(cls.getComponentType()).runtimeType);
            @SuppressWarnings("unchecked") // array is covariant
            TypeToken<? extends T> result = (TypeToken<? extends T>) of(arrayOfGenericType);
            return result;
        }
        TypeVariable<Class<T>>[] typeParams = cls.getTypeParameters();
        Type ownerType =
            cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers())
                ? toGenericType(cls.getEnclosingClass()).runtimeType
                : null;

        if ((typeParams.length > 0) || ((ownerType != null) && ownerType != cls.getEnclosingClass())) {
            @SuppressWarnings("unchecked") // Like, it's Iterable<T> for Iterable.class
            TypeToken<? extends T> type =
                (TypeToken<? extends T>)
                    of(Types.newParameterizedTypeWithOwner(ownerType, cls, typeParams));
            return type;
        } else {
            return of(cls);
        }
    }

    private TypeResolver getCovariantTypeResolver() {
        TypeResolver resolver = covariantTypeResolver;
        if (resolver == null) {
            resolver = (covariantTypeResolver = TypeResolver.covariantly(runtimeType));
        }
        return resolver;
    }

    private TypeResolver getInvariantTypeResolver() {
        TypeResolver resolver = invariantTypeResolver;
        if (resolver == null) {
            resolver = (invariantTypeResolver = TypeResolver.invariantly(runtimeType));
        }
        return resolver;
    }

    private TypeToken<? super T> getSupertypeFromUpperBounds(
        Class<? super T> supertype, Type[] upperBounds) {
        for (Type upperBound : upperBounds) {
            @SuppressWarnings("unchecked") // T's upperbound is <? super T>.
            TypeToken<? super T> bound = (TypeToken<? super T>) of(upperBound);
            if (bound.isSubtypeOf(supertype)) {
                @SuppressWarnings({"rawtypes", "unchecked"}) // guarded by the isSubtypeOf check.
                TypeToken<? super T> result = bound.getSupertype((Class) supertype);
                return result;
            }
        }
        throw new IllegalArgumentException(supertype + " isn't a super type of " + this);
    }

    private TypeToken<? extends T> getSubtypeFromLowerBounds(Class<?> subclass, Type[] lowerBounds) {
        if (lowerBounds.length > 0) {
            @SuppressWarnings("unchecked") // T's lower bound is <? extends T>
            TypeToken<? extends T> bound = (TypeToken<? extends T>) of(lowerBounds[0]);
            // Java supports only one lowerbound anyway.
            return bound.getSubtype(subclass);
        }
        throw new IllegalArgumentException(subclass + " isn't a subclass of " + this);
    }

    private TypeToken<? super T> getArraySupertype(Class<? super T> supertype) {
        // with component type, we have lost generic type information
        // Use raw type so that compiler allows us to call getSupertype()
        @SuppressWarnings("rawtypes")
        TypeToken componentType = getComponentType();
        // TODO(cpovirk): checkArgument?
        if (componentType == null) {
            throw new IllegalArgumentException(supertype + " isn't a super type of " + this);
        }
        // array is covariant. component type is super type, so is the array type.
        @SuppressWarnings("unchecked") // going from raw type back to generics
        /*
         * requireNonNull is safe because we call getArraySupertype only after checking
         * supertype.isArray().
         */
            TypeToken<?> componentSupertype =
            componentType.getSupertype(requireNonNull(supertype.getComponentType()));
        @SuppressWarnings("unchecked") // component type is super type, so is array type.
        TypeToken<? super T> result =
            (TypeToken<? super T>)
                // If we are passed with int[].class, don't turn it to GenericArrayType
                of(newArrayClassOrGenericArrayType(componentSupertype.runtimeType));
        return result;
    }

    private TypeToken<? extends T> getArraySubtype(Class<?> subclass) {
        Class<?> subclassComponentType = subclass.getComponentType();
        if (subclassComponentType == null) {
            throw new IllegalArgumentException(subclass + " does not appear to be a subtype of " + this);
        }
        // array is covariant. component type is subtype, so is the array type.
        // requireNonNull is safe because we call getArraySubtype only when isArray().
        TypeToken<?> componentSubtype =
            requireNonNull(getComponentType()).getSubtype(subclassComponentType);
        @SuppressWarnings("unchecked") // component type is subtype, so is array type.
        TypeToken<? extends T> result =
            (TypeToken<? extends T>)
                // If we are passed with int[].class, don't turn it to GenericArrayType
                of(newArrayClassOrGenericArrayType(componentSubtype.runtimeType));
        return result;
    }

    private Type resolveTypeArgsForSubclass(Class<?> subclass) {
        // If both runtimeType and subclass are not parameterized, return subclass
        // If runtimeType is not parameterized but subclass is, process subclass as a parameterized type
        // If runtimeType is a raw type (i.e. is a parameterized type specified as a Class<?>), we
        // return subclass as a raw type
        if (runtimeType instanceof Class
            && ((subclass.getTypeParameters().length == 0)
            || (getRawType().getTypeParameters().length != 0))) {
            // no resolution needed
            return subclass;
        }
        // class Base<A, B> {}
        // class Sub<X, Y> extends Base<X, Y> {}
        // Base<String, Integer>.subtype(Sub.class):

        // Sub<X, Y>.getSupertype(Base.class) => Base<X, Y>
        // => X=String, Y=Integer
        // => Sub<X, Y>=Sub<String, Integer>
        TypeToken<?> genericSubtype = toGenericType(subclass);
        @SuppressWarnings({"rawtypes", "unchecked"}) // subclass isn't <? extends T>
        Type supertypeWithArgsFromSubtype =
            genericSubtype.getSupertype((Class) getRawType()).runtimeType;
        return new TypeResolver()
            .where(supertypeWithArgsFromSubtype, runtimeType)
            .resolveType(genericSubtype.runtimeType);
    }

    private static Type newArrayClassOrGenericArrayType(Type componentType) {
        return Types.JavaVersion.JAVA7.newArrayType(componentType);
    }

    private static final class SimpleTypeToken<T> extends TypeToken<T> {

        SimpleTypeToken(Type type) {
            super(type);
        }

        private static final long serialVersionUID = 0;
    }

    private abstract static class TypeCollector<K> {

        static final TypeCollector<TypeToken<?>> FOR_GENERIC_TYPE =
            new TypeCollector<TypeToken<?>>() {
                @Override
                Class<?> getRawType(TypeToken<?> type) {
                    return type.getRawType();
                }

                @Override
                Iterable<? extends TypeToken<?>> getInterfaces(TypeToken<?> type) {
                    return type.getGenericInterfaces();
                }

                @Override
                TypeToken<?> getSuperclass(TypeToken<?> type) {
                    return type.getGenericSuperclass();
                }
            };

        static final TypeCollector<Class<?>> FOR_RAW_TYPE =
            new TypeCollector<Class<?>>() {
                @Override
                Class<?> getRawType(Class<?> type) {
                    return type;
                }

                @Override
                Iterable<? extends Class<?>> getInterfaces(Class<?> type) {
                    return Arrays.asList(type.getInterfaces());
                }

                @Override
                Class<?> getSuperclass(Class<?> type) {
                    return type.getSuperclass();
                }
            };

        final TypeCollector<K> classesOnly() {
            return new ForwardingTypeCollector<K>(this) {
                @Override
                Iterable<? extends K> getInterfaces(K type) {
                    return CollectionUtils.newHashSet();
                }

                @Override
                List<K> collectTypes(Iterable<? extends K> types) {
                    List<K> builder = CollectionUtils.newArrayList();
                    for (K type : types) {
                        if (!getRawType(type).isInterface()) {
                            builder.add(type);
                        }
                    }
                    return super.collectTypes(builder);
                }
            };
        }

        final List<K> collectTypes(K type) {
            ArrayList<K> list = new ArrayList<>();
            list.add(type);
            return collectTypes(list);
        }

        List<K> collectTypes(Iterable<? extends K> types) {
            // type -> order number. 1 for Object, 2 for anything directly below, so on so forth.
            Map<K, Integer> map = MapUtils.newHashMap();
            for (K type : types) {
                collectTypes(type, map);
            }
            return sortKeysByValue(map, (left, right) -> {
                checkNotNull(left); // right null is caught later
                if (left.equals(right)) {
                    return 0;
                }

                return ((Comparable<Integer>) right).compareTo(left);
            });
        }

        private int collectTypes(K type, Map<? super K, Integer> map) {
            Integer existing = map.get(type);
            if (existing != null) {
                // short circuit: if set contains type it already contains its supertypes
                return existing;
            }
            // Interfaces should be listed before Object.
            int aboveMe = getRawType(type).isInterface() ? 1 : 0;
            for (K interfaceType : getInterfaces(type)) {
                aboveMe = Math.max(aboveMe, collectTypes(interfaceType, map));
            }
            K superclass = getSuperclass(type);
            if (superclass != null) {
                aboveMe = Math.max(aboveMe, collectTypes(superclass, map));
            }
            map.put(type, aboveMe + 1);
            return aboveMe + 1;
        }

        private static <K, V> List<K> sortKeysByValue(Map<K, V> map, Comparator<? super V> valueComparator) {
            return map.keySet().stream()
                .sorted(((o1, o2) -> valueComparator.compare(
                    requireNonNull(map.get(o1)), requireNonNull(map.get(o2)))))
                .collect(Collectors.toList());
        }

        abstract Class<?> getRawType(K type);

        abstract Iterable<? extends K> getInterfaces(K type);

        abstract K getSuperclass(K type);

        private static class ForwardingTypeCollector<K> extends TypeCollector<K> {

            private final TypeCollector<K> delegate;

            ForwardingTypeCollector(TypeCollector<K> delegate) {
                this.delegate = delegate;
            }

            @Override
            Class<?> getRawType(K type) {
                return delegate.getRawType(type);
            }

            @Override
            Iterable<? extends K> getInterfaces(K type) {
                return delegate.getInterfaces(type);
            }

            @Override
            K getSuperclass(K type) {
                return delegate.getSuperclass(type);
            }
        }
    }

    // This happens to be the hash of the class as of now. So setting it makes a backward compatible
    // change. Going forward, if any incompatible change is added, we can change the UID back to 1.
    private static final long serialVersionUID = 3637540370352322684L;
}
