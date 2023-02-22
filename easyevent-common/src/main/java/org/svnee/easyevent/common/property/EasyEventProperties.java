package org.svnee.easyevent.common.property;

/**
 * EasyEvent Common Property
 *
 * @author svnee
 **/
public interface EasyEventProperties {

    /**
     * 应用ID
     *
     * @return appId
     */
    String getAppId();

    /**
     * 获取最大重试次数
     * @return 最大重试次数
     */
    Integer getMaxRetryCount();

}
