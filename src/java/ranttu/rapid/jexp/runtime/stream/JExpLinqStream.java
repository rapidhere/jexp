/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.experimental.var;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;

import java.util.stream.Stream;

/**
 * stream with linq extension
 *
 * @author rapid
 * @version : JExpLinqStream.java, v 0.1 2020-11-10 8:19 PM rapid Exp $
 */
public class JExpLinqStream extends DelegatedStream<OrderedTuple> {
    public static JExpLinqStream withName(Stream<?> stream, int idx) {
        return new JExpLinqStream(stream.map(v -> OrderedTuple.of(idx, v)));
    }

    JExpLinqStream(Stream<OrderedTuple> delegate) {
        super(delegate);
    }

    /**
     * select from stream
     */
    @SuppressWarnings("unused")
    public Stream<?> select(JExpFunctionHandle handle) {
        return map(tuple -> handle.invoke(tuple.toArray()));
    }

    /**
     * declare a shortcut
     */
    @SuppressWarnings("unused")
    public JExpLinqStream let(int idx, JExpFunctionHandle handle) {
        return new JExpLinqStream(map(tuple -> tuple.put(idx, handle.invoke(tuple.toArray()))));
    }

    /**
     * where clause
     */
    public JExpLinqStream where(JExpFunctionHandle handle) {
        return new JExpLinqStream(filter(
            tuple -> JExpLang.exactBoolean(handle.invoke(tuple.toArray()))));
    }

    /**
     * order by clause
     */
    public JExpLinqStream orderBy(JExpFunctionHandle[] keySelectorHandles, boolean[] descendingFlag) {
        return new JExpLinqStream(sorted((t1, t2) -> {
            var t1Keys = t1.toArray();
            var t2Keys = t2.toArray();
            var compRes = 0;

            for (var i = 0; i < keySelectorHandles.length; i++) {
                var handle = keySelectorHandles[i];
                var descending = descendingFlag[i];

                @SuppressWarnings("unchecked")
                var left = (Comparable<Object>) handle.invoke(t1Keys);
                var right = handle.invoke(t2Keys);

                compRes = left.compareTo(right);
                if (descending) {
                    compRes = -compRes;
                }

                if (compRes != 0) {
                    break;
                }
            }

            return compRes;
        }));
    }

    /**
     * join another stream
     */
    @SuppressWarnings("unused")
    public JExpLinqStream crossJoin(JExpLinqStream other) {
        var buff = StreamCache.of(other);

        Stream<OrderedTuple> stream = flatMap(thisValue ->
            buff.stream().map(otherValue -> OrderedTuple.merge(thisValue, otherValue)));

        return new JExpLinqStream(stream);
    }
}