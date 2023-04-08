package com.openquartz.easyevent.example.handler;

import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.event.TestEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;

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
