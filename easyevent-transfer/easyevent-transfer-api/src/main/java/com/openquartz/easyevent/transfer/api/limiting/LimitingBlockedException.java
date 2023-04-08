package com.openquartz.easyevent.transfer.api.limiting;

import com.openquartz.easyevent.common.exception.EasyEventErrorCode;
import com.openquartz.easyevent.common.exception.EasyEventException;

/**
 * LimitedBlockedException
 *
 * @author svnee
 **/
public class LimitingBlockedException extends EasyEventException {

    public LimitingBlockedException(EasyEventErrorCode errorCode) {
        super(errorCode);
    }

}
