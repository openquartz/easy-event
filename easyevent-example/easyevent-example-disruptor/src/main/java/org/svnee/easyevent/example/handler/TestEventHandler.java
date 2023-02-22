package org.svnee.easyevent.example.handler;

import org.svnee.easyevent.core.annotation.Subscribe;
import org.svnee.easyevent.example.event.TestEvent;
import org.svnee.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
public class TestEventHandler {

    @Subscribe
    public void handle(TestEvent testEvent){
        System.out.println(testEvent);
    }

}
