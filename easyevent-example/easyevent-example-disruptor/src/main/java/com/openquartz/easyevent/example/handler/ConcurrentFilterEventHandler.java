package com.openquartz.easyevent.example.handler;

import com.openquartz.easyevent.core.annotation.AllowConcurrentEvents;
import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.event.TestEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
public class ConcurrentFilterEventHandler {

    @AllowConcurrentEvents
    @Subscribe(condition = "args[0].source.equals(\'\')")
    public void handle(TestEvent event) {
        System.out.println(">>>>>>>>>--------Test2EventHandler" + event + ":" + Thread.currentThread().getId());
    }

}
