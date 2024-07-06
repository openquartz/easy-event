package com.openquartz.easyevent.example.soa.subcriber.handler;

import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.soa.event.OrderCompletedEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;

@EventHandler
public class OrderCompletedHandler {

    @Subscribe
    public void handle(OrderCompletedEvent event) {
        System.out.println(">------------- OrderCompletedHandler.handle" + event);
    }
}
