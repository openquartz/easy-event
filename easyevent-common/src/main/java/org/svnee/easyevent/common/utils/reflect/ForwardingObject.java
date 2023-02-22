package org.svnee.easyevent.common.utils.reflect;

/**
 * ForwardingObject
 *
 * @author svnee
 */
public abstract class ForwardingObject {

    protected ForwardingObject() {
    }

    protected abstract Object delegate();

    @Override
    public String toString() {
        return delegate().toString();
    }

}
