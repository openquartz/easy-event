package org.svnee.easyevent.core.compensate.translator;

import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.common.utils.StringUtils;
import org.svnee.easyevent.core.compensate.EventCompensateParam;
import org.svnee.easyevent.storage.model.BusEventSelectorCondition;

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
