package com.openquartz.easyevent.storage.identify;

/**
 * ID 生成 器
 *
 * @author svnee
 **/
public interface IdGenerator {

    /**
     * 生成ID
     * 如果返回null 代表使用数据库自增实现
     *
     * @param event 事件内容
     *
     * @return ID
     */
    default <T> Long generateId(T event) {
        return null;
    }

}
