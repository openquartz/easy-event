package org.svnee.easyevent.transfer.api.limiting;

import org.svnee.easyevent.common.exception.EasyEventErrorCode;
import org.svnee.easyevent.common.exception.EasyEventException;

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
