package com.openquartz.easyevent.example.event;

import com.openquartz.easyevent.core.IEvent;
import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class TestEvent implements IEvent {

    private String source;

    public TestEvent() {
    }

    public TestEvent(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    @Override
    public String getEventKey() {
        return source;
    }
}
