package com.openquartz.easyevent.common.utils;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Joiner to String
 *
 * @author svnee
 **/
public final class Joiner {

    private Joiner() {
    }

    /**
     * join
     *
     * @param col col
     * @param splitter splitter
     * @return join result
     */
    public static String join(Collection<?> col, String splitter) {

        checkNotEmpty(splitter);

        if (CollectionUtils.isEmpty(col)) {
            return StringUtils.EMPTY;
        }

        StringJoiner joiner = new StringJoiner(splitter);
        for (Object o : col) {
            joiner.add(String.valueOf(o));
        }
        return joiner.toString();
    }

    public static String join(String splitter, Object... obj) {

        checkNotEmpty(splitter);

        if (obj == null || obj.length == 0) {
            return StringUtils.EMPTY;
        }

        StringJoiner joiner = new StringJoiner(splitter);
        for (Object o : obj) {
            joiner.add(String.valueOf(o));
        }
        return joiner.toString();
    }

    /**
     * 分割
     *
     * @param source source
     * @param splitter splitter-character
     * @return split result
     */
    public static List<String> split(String source, String splitter) {

        checkNotEmpty(splitter);

        if (StringUtils.isBlank(source)) {
            return Collections.emptyList();
        }

        return Arrays.stream(source.trim().split(splitter))
            .map(String::trim)
            .collect(Collectors.toList());
    }
}
