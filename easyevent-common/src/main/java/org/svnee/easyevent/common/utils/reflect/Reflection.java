/*
 * Copyright (C) 2005 The Guava Authors
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

import static org.svnee.easyevent.common.utils.ParamUtils.checkArgument;
import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Static utilities relating to Java reflection.
 *
 * @since 1.0.0
 */
public final class Reflection {

    private Reflection() {
    }

    public static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {

        checkNotNull(handler);
        checkArgument(interfaceType.isInterface());

        Object object =
            Proxy.newProxyInstance(
                interfaceType.getClassLoader(), new Class<?>[]{interfaceType}, handler);
        return interfaceType.cast(object);
    }


}
