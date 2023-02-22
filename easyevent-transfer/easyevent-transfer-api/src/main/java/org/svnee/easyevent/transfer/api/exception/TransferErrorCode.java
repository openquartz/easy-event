package org.svnee.easyevent.transfer.api.exception;

import lombok.Getter;
import org.svnee.easyevent.common.exception.EasyEventErrorCode;

/**
 * TransferErrorCode
 *
 * @author svnee
 */
@Getter
public enum TransferErrorCode implements EasyEventErrorCode {

    SENDER_FAILED("01", "消息发送失败!msgId:{0},topic:{1},tag:{2}", true),

    ;

    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "TransferError-";

    TransferErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    TransferErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }

}
