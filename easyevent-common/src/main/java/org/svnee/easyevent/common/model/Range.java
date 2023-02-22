package org.svnee.easyevent.common.model;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.Objects;
import org.svnee.easyevent.common.exception.CommonErrorCode;
import org.svnee.easyevent.common.utils.Asserts;

/**
 * Range
 *
 * @author svnee
 **/
public class Range<K> {

    private K start;
    private K end;

    public Range(K start, K end) {

        Asserts.isTrue(Objects.nonNull(start) || Objects.nonNull(end), CommonErrorCode.PARAM_ILLEGAL_ERROR);

        this.start = start;
        this.end = end;
    }

    public K getStart() {
        return start;
    }

    public K getEnd() {
        return end;
    }

    public boolean startIfAbsent(K start) {

        checkNotNull(start);

        if (getStart() == null) {
            this.start = start;
            return true;
        }
        return false;
    }

    public void start(K start) {

        checkNotNull(start);

        this.start = start;
    }

    public void end(K end) {

        checkNotNull(end);

        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Range<?> range = (Range<?>) o;
        return Objects.equals(start, range.start) && Objects.equals(end, range.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
