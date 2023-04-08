package com.openquartz.easyevent.common.utils.reflect;

import java.util.Set;

/**
 * forwarding set
 *
 * @param <E> E
 * @author svnee
 */
public abstract class ForwardingSet<E extends Object> extends ForwardingCollection<E>
    implements Set<E> {

    /** Constructor for use by subclasses. */
    protected ForwardingSet() {
    }

    @Override
    protected abstract Set<E> delegate();

    @Override
    public boolean equals(Object object) {
        return object == this || delegate().equals(object);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }
}
