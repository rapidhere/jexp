/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.experimental.var;
import ranttu.rapid.jexp.JExpFunctionHandle;

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
    public Stream<?> select(JExpFunctionHandle handle) {
        return map(tuple -> handle.invoke(tuple.toArray()));
    }

    /**
     * join another stream
     */
    public JExpLinqStream crossJoin(JExpLinqStream other) {
        var buff = StreamCache.of(other);

        Stream<OrderedTuple> stream = flatMap(thisValue ->
            buff.stream().map(otherValue -> OrderedTuple.merge(thisValue, otherValue)));

        return new JExpLinqStream(stream);
    }
}