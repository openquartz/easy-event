package org.svnee.easyevent.example.identify;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import org.svnee.easyevent.storage.identify.IdGenerator;

/**
 * @author svnee
 **/
@Component
public class LocalIdGenerator implements IdGenerator {

    private final AtomicLong id = new AtomicLong(10);

    @Override
    public <T> Long generateId(T event) {
        return id.incrementAndGet();
    }
}
