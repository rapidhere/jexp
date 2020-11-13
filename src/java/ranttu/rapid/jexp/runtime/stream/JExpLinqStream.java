/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.experimental.var;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;
import ranttu.rapid.jexp.runtime.function.builtin.StreamFunctions;

import java.util.stream.Collectors;
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
        super(() -> delegate);
    }

    /**
     * select from stream
     */
    @SuppressWarnings("unused")
    public Stream<?> select(JExpFunctionHandle handle) {
        return map(tuple -> handle.invoke(tuple.toArray()));
    }

    /**
     * group the stream
     */
    @SuppressWarnings("unused")
    public Grouping<?, ?> group(JExpFunctionHandle selectHandle, JExpFunctionHandle keyHandle) {
        return Grouping.ofGrouped(() ->
            collect(Collectors.groupingBy(
                tuple -> keyHandle.invoke(tuple.toArray()),
                Collectors.mapping(
                    tuple -> selectHandle.invoke(tuple.toArray()),
                    Collectors.toList()
                )
            )));
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
    @SuppressWarnings("unused")
    public JExpLinqStream where(JExpFunctionHandle handle) {
        return new JExpLinqStream(filter(
            tuple -> JExpLang.exactBoolean(handle.invoke(tuple.toArray()))));
    }

    /**
     * order by clause
     */
    @SuppressWarnings("unused")
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
     * group join
     */
    @SuppressWarnings("unused")
    public JExpLinqStream groupJoin(JExpLinqStream inner, JExpFunctionHandle outerKeySelector,
                                    JExpFunctionHandle innerKeySelector, int groupIdx, int innerItemIdx) {
        var kvBuff = StreamCache.of(inner
            .map(v -> new KVPair(innerKeySelector.invoke(v.toArray()), v.get(innerItemIdx))));

        Stream<OrderedTuple> stream = map(outerValue -> {
            var outerKey = outerKeySelector.invoke(outerValue.toArray());
            var innerStream = kvBuff.stream()
                .filter(innerKV -> JExpLang.eq(outerKey, innerKV.key))
                .map(innerKV -> innerKV.value);

            return outerValue.put(groupIdx, innerStream);
        });

        return new JExpLinqStream(stream);
    }

    /**
     * inner join
     */
    @SuppressWarnings("unused")
    public JExpLinqStream /*inner-equi*/join(JExpLinqStream inner,
                                             JExpFunctionHandle outerKeySelector,
                                             JExpFunctionHandle innerKeySelector) {
        var kvBuff = StreamCache.of(inner
            .map(v -> new KVPair(innerKeySelector.invoke(v.toArray()), v)));

        Stream<OrderedTuple> stream = flatMap(outerValue -> {
            var outerKey = outerKeySelector.invoke(outerValue.toArray());

            return kvBuff.stream()
                .filter(innerKV -> JExpLang.eq(outerKey, innerKV.key))
                .map(innerKV -> OrderedTuple.merge(outerValue, (OrderedTuple) innerKV.value));
        });

        return new JExpLinqStream(stream);
    }

    /**
     * join another stream
     */
    @SuppressWarnings("unused")
    public JExpLinqStream crossJoinStatic(int nameIdx, JExpFunctionHandle otherSourceHandle) {
        var buff = StreamCache.of(
            StreamFunctions.withName(nameIdx, otherSourceHandle.invoke(new Object[0])));

        Stream<OrderedTuple> stream = flatMap(thisValue ->
            buff.stream().map(otherValue -> OrderedTuple.merge(thisValue, otherValue)));

        return new JExpLinqStream(stream);
    }

    /**
     * join another stream, dynamic
     */
    @SuppressWarnings("unused")
    public JExpLinqStream crossJoinDynamic(int nameIdx, JExpFunctionHandle otherSourceHandle) {
        Stream<OrderedTuple> stream = flatMap(thisValue ->
            StreamFunctions
                .withName(nameIdx, otherSourceHandle.invoke(thisValue.toArray()))
                .map(otherValue -> OrderedTuple.merge(thisValue, otherValue))
        );

        return new JExpLinqStream(stream);
    }
}