package org.svnee.easyevent.common.concurrent.lock;

import org.svnee.easyevent.common.model.EnumInterface;

/**
 * LockBizType
 *
 * @author svnee
 */
public enum LockBizType implements EnumInterface<String> {

    EVENT_HANDLE("event_handle", "event handler"),
    EVENT_HANDLE_FAIL_NOTIFIER("event_handle_fail_notifier", "event handle fail notifier"),
    ;

    private final String code;
    private final String desc;

    LockBizType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
