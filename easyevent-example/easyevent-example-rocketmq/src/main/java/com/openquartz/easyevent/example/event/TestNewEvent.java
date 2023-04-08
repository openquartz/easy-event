package com.openquartz.easyevent.example.event;

/**
 * @author svnee
 **/
public class TestNewEvent {

    private Integer price;

    public TestNewEvent() {
    }

    public TestNewEvent(Integer price) {
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
