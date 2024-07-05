package com.openquartz.easyevent.example.soa.event;

import com.openquartz.easyevent.example.soa.event.constants.SoaIdentifyConstants;
import com.openquartz.easyevent.starter.soa.api.SoaEvent;
import lombok.Data;

/**
 * 单据完成事件
 *
 * @author svnee
 */
@Data
public class OrderCompletedEvent implements SoaEvent {

    /**
     * 单号
     */
    private String orderNo;

    /**
     * 完成时间-timestamp
     */
    private Long completedTime;

    @Override
    public String getSoaIdentify() {
        return SoaIdentifyConstants.ORDER_SERVICE_APPID;
    }
}
