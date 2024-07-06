package com.openquartz.easyevent.example.soa.publisher.controller;

import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.example.soa.event.OrderCompletedEvent;
import com.openquartz.easyevent.example.soa.publisher.controller.dto.CompleteOrderRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private EventPublisher eventPublisher;

    @PostMapping("/complete")
    public String complete(@RequestBody CompleteOrderRequest completeRequest) {

        OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent();
        orderCompletedEvent.setOrderNo(completeRequest.getOrderId());
        orderCompletedEvent.setCompletedTime(System.currentTimeMillis());
        eventPublisher.asyncPublish(orderCompletedEvent);
        return "success";
    }
}
