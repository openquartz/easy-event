package org.svnee.easyevent.common.utils.reflect;

import java.util.Collection;
import java.util.Iterator;

/**
 * ForwardingCollection
 *
 * @author svnee
 */
public abstract class ForwardingCollection<E extends Object> extends ForwardingObject
    implements Collection<E> {

    protected ForwardingCollection() {
    }

    @Override
    protected abstract Collection<E> delegate();

    @Override
    public Iterator<E> iterator() {
        return delegate().iterator();
    }

    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return delegate().removeAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        return delegate().contains(object);
    }

    @Override
    public boolean add(E element) {
        return delegate().add(element);
    }

    @Override
    public boolean remove(Object object) {
        return delegate().remove(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return delegate().containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return delegate().addAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return delegate().retainAll(collection);
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    @Override
    public Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    @SuppressWarnings("nullness")
    public <T extends Object> T[] toArray(T[] array) {
        return delegate().toArray(array);
    }
}
