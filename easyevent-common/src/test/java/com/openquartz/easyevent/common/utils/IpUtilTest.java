package com.openquartz.easyevent.common.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class IpUtilTest {

    @Test
    public void getIp_shouldNeverReturnNull() {
        String ip = IpUtil.getIp();
        assertNotNull(ip);
        assertFalse(ip.trim().isEmpty());
    }
}
