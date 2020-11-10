/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import ranttu.rapid.jexp.runtime.function.JExpFunction;
import ranttu.rapid.jexp.runtime.stream.JExpLinqStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * stream helpers
 *
 * @author rapid
 * @version : Streams.java, v 0.1 2020-11-09 9:32 PM rapid Exp $
 */
final public class StreamFunctions {
    private StreamFunctions() {
        throw new UnsupportedOperationException();
    }

    @JExpFunction(lib = "stream", name = "of")
    public static JExpLinqStream withName(int idx, Object o) {
        Stream<?> stream;
        if (o instanceof Collection<?>) {
            stream = ((Collection<?>) o).stream();
        } else if (o instanceof Map<?, ?>) {
            stream = ((Map<?, ?>) o).entrySet().stream();
        } else if (o instanceof Object[]) {
            // TODO: support other array
            stream = Arrays.stream((Object[]) o);
        } else if (o instanceof CharSequence) {
            stream = ((CharSequence) o).chars().boxed().map(i -> (char) i.intValue());
        } else {
            stream = (o == null ? Stream.empty() : Stream.of(o));
        }

        return JExpLinqStream.withName(stream, idx);
    }
}