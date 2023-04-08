package com.openquartz.easyevent.core.compensate.translator;

import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.core.compensate.EventCompensateParam;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;

/**
 * EventCompensateParamTranslator
 *
 * @author svnee
 **/
public final class EventCompensateParamTranslator {

    private EventCompensateParamTranslator() {
    }

    public static BusEventSelectorCondition translate(EventCompensateParam compensateParam) {
        BusEventSelectorCondition condition = BusEventSelectorCondition
            .builder(compensateParam.getOffset())
            .lifecycleState(compensateParam.getCompensateState())
            .start(compensateParam.getStartTime())
            .end(compensateParam.getEndTime())
            .maxErrorCount(compensateParam.getMaxErrorCount());
        if (StringUtils.isNotBlank(compensateParam.getIpAddr())) {
            condition.creatingOwner(CollectionUtils.newArrayList(compensateParam.getIpAddr()));
        }
        return condition;
    }

}
