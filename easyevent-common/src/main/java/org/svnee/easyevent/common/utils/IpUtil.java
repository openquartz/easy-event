package org.svnee.easyevent.common.utils;

import java.net.InetAddress;

/**
 * IP 工具类
 *
 * @author svnee
 */
public final class IpUtil {

    private IpUtil() {
    }

    /**
     * 获取当前机器的IP
     *
     * @return ip
     */
    public static String getIp() {
        // 查询当前IP
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        return ip;
    }

}
