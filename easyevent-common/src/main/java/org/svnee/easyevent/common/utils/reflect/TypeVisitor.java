/*
 * Copyright (C) 2013 The Guava Authors
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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Set;
import org.svnee.easyevent.common.utils.CollectionUtils;

abstract class TypeVisitor {

    private final Set<Type> visited = CollectionUtils.newHashSet();

    /**
     * Visits the given types. Null types are ignored. This allows subclasses to call {@code
     * visit(parameterizedType.getOwnerType())} safely without having to check nulls.
     */
    public final void visit(Type... types) {
        for (Type type : types) {
            if (type == null || !visited.add(type)) {
                continue;
            }
            boolean succeeded = false;
            try {
                if (type instanceof TypeVariable) {
                    visitTypeVariable((TypeVariable<?>) type);
                } else if (type instanceof WildcardType) {
                    visitWildcardType((WildcardType) type);
                } else if (type instanceof ParameterizedType) {
                    visitParameterizedType((ParameterizedType) type);
                } else if (type instanceof Class) {
                    visitClass((Class<?>) type);
                } else if (type instanceof GenericArrayType) {
                    visitGenericArrayType((GenericArrayType) type);
                } else {
                    throw new AssertionError("Unknown type: " + type);
                }
                succeeded = true;
            } finally {
                if (!succeeded) {
                    visited.remove(type);
                }
            }
        }
    }

    void visitClass(Class<?> t) {
    }

    void visitGenericArrayType(GenericArrayType t) {
    }

    void visitParameterizedType(ParameterizedType t) {
    }

    void visitTypeVariable(TypeVariable<?> t) {
    }

    void visitWildcardType(WildcardType t) {
    }
}
