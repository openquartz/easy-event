package org.svnee.easyevent.starter.schedule;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * ScheduleCompensateRejectedExecutionHandler
 *
 * @author svnee
 */
@Slf4j
public class ScheduleCompensateRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

    }
}
