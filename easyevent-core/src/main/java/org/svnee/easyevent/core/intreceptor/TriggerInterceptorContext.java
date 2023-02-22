package org.svnee.easyevent.core.intreceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文
 *
 * @author svnee
 **/
public class TriggerInterceptorContext {

    private final Map<String, Object> paramMap = new ConcurrentHashMap<>();

    public void putParam(String key, Object param) {
        paramMap.put(key, param);
    }

    public Object get(String key) {
        if (!paramMap.containsKey(key)) {
            return null;
        }
        return paramMap.get(key);
    }

}
