package com.openquartz.easyevent.example.handler;

import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.event.TestEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EventHandler
public class RpcEventHandler {

    @Subscribe(joinTransaction = false)
    public void handle(TestEvent event) throws InterruptedException {
        log.info(">>>>>>>>>--------RpcEventHandler" + event + ":" + Thread.currentThread().getId());
        Thread.sleep(10000L);
    }
}
