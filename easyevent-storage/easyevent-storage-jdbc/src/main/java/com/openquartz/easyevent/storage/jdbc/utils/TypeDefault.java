package com.openquartz.easyevent.storage.jdbc.utils;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openquartz.easyevent.common.utils.StringUtils;

/**
 * Condition
 *
 * @author svnee
 **/
public class TypeDefault {

    private final Map<Class<?>, TypeDefaultVal<?>> typeDefaultMap;

    public static final TypeDefault DEFAULT;

    static {
        DEFAULT = new TypeDefault();
        DEFAULT.append(new TypeDefaultVal<>(String.class, StringUtils.EMPTY));
        DEFAULT.append(new TypeDefaultVal<>(Integer.class, 0));
        DEFAULT.append(new TypeDefaultVal<>(Long.class, 0L));
    }

    public TypeDefault() {
        this.typeDefaultMap = new LinkedHashMap<>();
    }

    public TypeDefault append(TypeDefaultVal<?> typeDefaultVal) {

        checkNotNull(typeDefaultVal);

        typeDefaultMap.putIfAbsent(typeDefaultVal.getType(), typeDefaultVal);
        return this;
    }

    public Object matchValue(Class<?> type) {
        return typeDefaultMap.entrySet().stream()
            .filter(e -> e.getKey().isAssignableFrom(type))
            .min(Comparator.comparing(o -> o.getValue().getOrder()))
            .map(e -> e.getValue().getValue())
            .orElse(null);
    }
}
