package com.openquartz.easyevent.common.model;

/**
 * LifecycleBean
 *
 * @author svnee
 */
public interface LifecycleBean {

    /**
     * init
     */
    default void init(){
    }

    /**
     * destroy method
     */
    default void destroy(){

    }
}
