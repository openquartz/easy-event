package com.openquartz.easyevent.common.serde;

import com.openquartz.easyevent.common.utils.JSONUtil;

/**
 * Serializer
 *
 * @author svnee
 */
public interface Serializer {

    Serializer DEFAULT = new DefaultSerializer();

    /**
     * 序列化
     *
     * @param object 序列化对象
     * @return 序列化结果
     */
    String serialize(Object object);

    /**
     * 反序列化
     *
     * @param type 类型
     * @param str 序列化的结果
     * @param <T> 反序列化类型
     * @return 反序列化结果
     */
    <T> T deserialize(Class<T> type, String str);

    class DefaultSerializer implements Serializer {

        @Override
        public String serialize(Object object) {
            return JSONUtil.toJson(object);
        }

        @Override
        public <T> T deserialize(Class<T> type, String str) {
            return JSONUtil.parseObject(str, type);
        }
    }
}
