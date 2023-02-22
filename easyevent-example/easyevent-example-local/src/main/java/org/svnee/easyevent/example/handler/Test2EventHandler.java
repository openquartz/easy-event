package org.svnee.easyevent.example.handler;

import org.svnee.easyevent.core.annotation.AllowConcurrentEvents;
import org.svnee.easyevent.core.annotation.Subscribe;
import org.svnee.easyevent.example.event.TestEvent;
import org.svnee.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
public class Test2EventHandler {

    @Subscribe
    @AllowConcurrentEvents
    public void handle(TestEvent event) {
        throw new RuntimeException("xxxx");
    }

}
