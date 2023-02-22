package org.svnee.easyevent.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 集合工具类
 *
 * @author svnee
 **/
public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * new hashset
     *
     * @param <T> T
     * @return set
     */
    public static <T> Set<T> newHashSet() {
        return new HashSet<>();
    }

    /**
     * Helper class to easily access cardinality properties of two collections.
     *
     * @param <O> the element type
     */
    private static class CardinalityHelper<O> {

        /** Contains the cardinality for each object in collection A. */
        final Map<O, Integer> cardinalityA;

        /** Contains the cardinality for each object in collection B. */
        final Map<O, Integer> cardinalityB;

        /**
         * Create a new CardinalityHelper for two collections.
         *
         * @param a the first collection
         * @param b the second collection
         */
        public CardinalityHelper(final Iterable<? extends O> a, final Iterable<? extends O> b) {
            cardinalityA = CollectionUtils.getCardinalityMap(a);
            cardinalityB = CollectionUtils.getCardinalityMap(b);
        }

        /**
         * Returns the maximum frequency of an object.
         *
         * @param obj the object
         * @return the maximum frequency of the object
         */
        public final int max(final Object obj) {
            return Math.max(freqA(obj), freqB(obj));
        }

        /**
         * Returns the minimum frequency of an object.
         *
         * @param obj the object
         * @return the minimum frequency of the object
         */
        public final int min(final Object obj) {
            return Math.min(freqA(obj), freqB(obj));
        }

        /**
         * Returns the frequency of this object in collection A.
         *
         * @param obj the object
         * @return the frequency of the object in collection A
         */
        public int freqA(final Object obj) {
            return getFreq(obj, cardinalityA);
        }

        /**
         * Returns the frequency of this object in collection B.
         *
         * @param obj the object
         * @return the frequency of the object in collection B
         */
        public int freqB(final Object obj) {
            return getFreq(obj, cardinalityB);
        }

        private int getFreq(final Object obj, final Map<?, Integer> freqMap) {
            final Integer count = freqMap.get(obj);
            if (count != null) {
                return count;
            }
            return 0;
        }
    }

    public static <C> boolean addAll(final Collection<C> collection, final Iterable<? extends C> iterable) {
        if (iterable instanceof Collection<?>) {
            return collection.addAll((Collection<? extends C>) iterable);
        }
        return addAll(collection, iterable.iterator());
    }

    public static <C> boolean addAll(final Collection<C> collection, final Iterator<? extends C> iterator) {
        boolean changed = false;
        while (iterator.hasNext()) {
            changed |= collection.add(iterator.next());
        }
        return changed;
    }


    /**
     * Helper class for set-related operations, e.g. union, subtract, intersection.
     *
     * @param <O> the element type
     */
    private static class SetOperationCardinalityHelper<O> extends CardinalityHelper<O> implements Iterable<O> {

        /** Contains the unique elements of the two collections. */
        private final Set<O> elements;

        /** Output collection. */
        private final List<O> newList;

        /**
         * Create a new set operation helper from the two collections.
         *
         * @param a the first collection
         * @param b the second collection
         */
        public SetOperationCardinalityHelper(final Iterable<? extends O> a, final Iterable<? extends O> b) {
            super(a, b);
            elements = new HashSet<>();
            addAll(elements, a);
            addAll(elements, b);
            // the resulting list must contain at least each unique element, but may grow
            newList = new ArrayList<>(elements.size());
        }

        @Override
        public Iterator<O> iterator() {
            return elements.iterator();
        }

        /**
         * Add the object {@code count} times to the result collection.
         *
         * @param obj the object to add
         * @param count the count
         */
        public void setCardinality(final O obj, final int count) {
            for (int i = 0; i < count; i++) {
                newList.add(obj);
            }
        }

        /**
         * Returns the resulting collection.
         *
         * @return the result
         */
        public Collection<O> list() {
            return newList;
        }

    }

    public static <O> Collection<O> intersection(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.min(obj));
        }
        return helper.list();
    }

    /**
     * 是否为空
     *
     * @param coll 空集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * 是否为非空
     *
     * @param coll 空集合
     * @return 是否为非空集合
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * Returns a {@link Map} mapping each unique element in the given
     * {@link Collection} to an {@link Integer} representing the number
     * of occurrences of that element in the {@link Collection}.
     * <p>
     * Only those elements present in the collection will appear as
     * keys in the map.
     *
     * @param <O> the type of object in the returned {@link Map}. This is a super type of <I>.
     * @param coll the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    public static <O> Map<O, Integer> getCardinalityMap(final Iterable<? extends O> coll) {
        final Map<O, Integer> count = new HashMap<>();
        for (final O obj : coll) {
            count.merge(obj, 1, Integer::sum);
        }
        return count;
    }

    /**
     * 创建ArrayList
     *
     * @param collection 集合
     * @param <T> T
     * @return list
     */
    public static <T> List<T> newArrayList(Collection<T> collection) {
        if (Objects.isNull(collection)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(collection);
    }

    public static <T> List<T> newArrayList(T... elements) {
        checkNotNull(elements);
        int capacity = computeArrayListCapacity(elements.length);
        List<T> list = new ArrayList<>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }


    static int computeArrayListCapacity(int arraySize) {
        if (arraySize < 0) {
            throw new IllegalArgumentException("arrayList cannot be negative but was: " + arraySize);
        }
        return saturatedCast(5L + arraySize + (arraySize / 10));
    }

    private static int saturatedCast(long value) {
        if (value > 2147483647L) {
            return 2147483647;
        } else {
            return value < -2147483648L ? -2147483648 : (int) value;
        }
    }

    private static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static <T> T getFirst(Collection<T> collection) {
        return Optional.ofNullable(collection)
            .orElse(Collections.emptyList())
            .stream()
            .findFirst()
            .orElse(null);
    }

    public static <T> List<T> merge(Collection<T> first, Collection<T> second) {
        int capacity = (Objects.nonNull(first) ? first.size() : 0) + (Objects.nonNull(second) ? second.size() : 0);
        List<T> resultList = new ArrayList<>(capacity);
        if (CollectionUtils.isNotEmpty(first)) {
            resultList.addAll(first);
        }
        if (CollectionUtils.isNotEmpty(second)) {
            resultList.addAll(second);
        }
        return resultList;
    }

    /**
     * 无重复合并集合
     *
     * @param first first-col
     * @param second second-col
     * @param <T> T
     * @return merge (no repeat) result
     */
    public static <T> List<T> mergeNoRepeat(Collection<T> first, Collection<T> second) {
        Set<T> set = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(first)) {
            set.addAll(first);
        }
        if (CollectionUtils.isNotEmpty(second)) {
            set.addAll(second);
        }
        return new ArrayList<>(set);
    }

    /**
     * copy to set by source list
     *
     * @param source source
     * @param <T> T
     * @return SET
     */
    public static <T> Set<T> copyToSetOf(List<T> source) {
        return new HashSet<>(source);
    }

    /**
     * concat
     *
     * @param source source
     * @param <T> T
     * @return iterator
     */
    public static <T> Iterator<T> concat(Iterator<Iterator<T>> source) {
        if (source == null || !source.hasNext()) {
            return Collections.emptyIterator();
        }
        List<T> result = new ArrayList<>();
        while (source.hasNext()) {
            Iterator<T> iterator = source.next();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
        return result.iterator();
    }
}
