package org.svnee.easyevent.example.handler;

import org.svnee.easyevent.core.annotation.AllowConcurrentEvents;
import org.svnee.easyevent.core.annotation.Subscribe;
import org.svnee.easyevent.example.event.TestEvent;
import org.svnee.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
public class TestEventHandler {

    @Subscribe
    public void handle(TestEvent testEvent) {
        throw new RuntimeException("xxxxxxx--test");
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handle2(TestEvent event){
        System.out.println("----xxx-------");
    }

}
