package com.openquartz.easyevent.starter.soa.api;

/**
 * SoaEvent
 *
 * @author xuzhao
 */
public interface SoaEvent {

    /**
     * soa Identify
     *
     * @return soa Identify
     */
    String getSoaIdentify();

    /**
     * 事件key 用于soa 检索
     * @return eventKey
     */
    default String getEventKey() {
        return null;
    }
}