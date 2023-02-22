package org.svnee.easyevent.storage.identify;

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
     * @return ID
     */
    default Long generateId() {
        return null;
    }

}
