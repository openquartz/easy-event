package com.openquartz.easyevent.example.handler;

import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.event.TestEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author svnee
 **/
@Slf4j
@EventHandler
public class JoinMainTransactionEventHandler {

    @Subscribe
    public void handle(TestEvent event) {
        log.info(">>>>>>>>>--------TestEventHandler" + event + ":" + Thread.currentThread().getId());
    }

}
