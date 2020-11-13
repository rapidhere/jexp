/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.experimental.var;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.runtime.function.JExpExtensionMethod;
import ranttu.rapid.jexp.runtime.function.JExpFunction;
import ranttu.rapid.jexp.runtime.function.This;
import ranttu.rapid.jexp.runtime.function.builtin.JExpLang;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public static Stream<OrderedTuple> withName(int idx, Object o) {
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
        } else if (o instanceof Stream) {
            stream = (Stream<?>) o;
        } else {
            stream = (o == null ? Stream.empty() : Stream.of(o));
        }

        return stream.map(v -> OrderedTuple.of(idx, v));
    }

    //~~~ default stream extensions
    @JExpExtensionMethod
    @JExpFunction(lib = "stream", name = "to_list")
    public static <T> List<T> toList(@This Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    //~~~ linq extensions

    /**
     * select from stream
     */
    @JExpExtensionMethod(name = "linqSelect")
    @JExpFunction(lib = "linqimpl", name = "select")
    public static Stream<?> select(@This Stream<OrderedTuple> s, JExpFunctionHandle handle) {
        return s.map(tuple -> handle.invoke(tuple.toArray()));
    }

    /**
     * group the stream
     */
    @JExpExtensionMethod(name = "linqGroup")
    @JExpFunction(lib = "linqimpl", name = "group")
    public static Grouping<?, ?> group(@This Stream<OrderedTuple> s,
                                       JExpFunctionHandle selectHandle,
                                       JExpFunctionHandle keyHandle) {
        return Grouping.ofGrouped(() ->
            s.collect(Collectors.groupingBy(
                tuple -> keyHandle.invoke(tuple.toArray()),
                Collectors.mapping(
                    tuple -> selectHandle.invoke(tuple.toArray()),
                    Collectors.toList()
                )
            ))
        );
    }

    /**
     * declare a shortcut
     */
    @JExpExtensionMethod(name = "linqLet")
    @JExpFunction(lib = "linqimpl", name = "let")
    public static Stream<OrderedTuple> let(@This Stream<OrderedTuple> s, int idx, JExpFunctionHandle handle) {
        return s.map(tuple -> tuple.put(idx, handle.invoke(tuple.toArray())));
    }

    /**
     * where clause
     */
    @JExpExtensionMethod(name = "linqWhere")
    @JExpFunction(lib = "linqimpl", name = "where")
    public static Stream<OrderedTuple> where(@This Stream<OrderedTuple> s, JExpFunctionHandle handle) {
        return s.filter(
            tuple -> JExpLang.exactBoolean(handle.invoke(tuple.toArray())));
    }

    /**
     * order by clause
     */
    @JExpExtensionMethod(name = "linqOrderby")
    @JExpFunction(lib = "linqimpl", name = "orderby")
    public static Stream<OrderedTuple> orderBy(@This Stream<OrderedTuple> s,
                                               JExpFunctionHandle[] keySelectorHandles,
                                               boolean[] descendingFlag) {
        return s.sorted((t1, t2) -> {
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
        });
    }

    /**
     * group join
     */
    @JExpExtensionMethod(name = "linqGroupJoin")
    @JExpFunction(lib = "linqimpl", name = "group_join")
    public static Stream<OrderedTuple> groupJoin(@This Stream<OrderedTuple> outer,
                                                 Stream<OrderedTuple> inner,
                                                 JExpFunctionHandle outerKeySelector,
                                                 JExpFunctionHandle innerKeySelector,
                                                 int groupIdx, int innerItemIdx) {
        var kvBuff = StreamCache.of(inner
            .map(v -> new KVPair(innerKeySelector.invoke(v.toArray()), v.get(innerItemIdx))));

        return outer.map(outerValue -> {
            var outerKey = outerKeySelector.invoke(outerValue.toArray());
            var innerStream = kvBuff.stream()
                .filter(innerKV -> JExpLang.eq(outerKey, innerKV.key))
                .map(innerKV -> innerKV.value);

            return outerValue.put(groupIdx, innerStream);
        });
    }

    /**
     * inner join
     */
    @JExpExtensionMethod(name = "linqEquiJoin")
    @JExpFunction(lib = "linqimpl", name = "equi_join")
    public static Stream<OrderedTuple> /*inner-equi*/join(@This Stream<OrderedTuple> outer,
                                                          Stream<OrderedTuple> inner,
                                                          JExpFunctionHandle outerKeySelector,
                                                          JExpFunctionHandle innerKeySelector) {
        var kvBuff = StreamCache.of(inner
            .map(v -> new KVPair(innerKeySelector.invoke(v.toArray()), v)));

        return outer.flatMap(outerValue -> {
            var outerKey = outerKeySelector.invoke(outerValue.toArray());

            return kvBuff.stream()
                .filter(innerKV -> JExpLang.eq(outerKey, innerKV.key))
                .map(innerKV -> OrderedTuple.merge(outerValue, (OrderedTuple) innerKV.value));
        });
    }

    /**
     * join another stream
     */
    @JExpExtensionMethod(name = "linqCrossJoinStatic")
    @JExpFunction(lib = "linqimpl", name = "cross_join_static")
    public static Stream<OrderedTuple> crossJoinStatic(
        @This Stream<OrderedTuple> s, int nameIdx, JExpFunctionHandle otherSourceHandle) {
        var buff = StreamCache.of(
            StreamFunctions.withName(nameIdx, otherSourceHandle.invoke(new Object[0])));

        return s.flatMap(thisValue ->
            buff.stream().map(otherValue -> OrderedTuple.merge(thisValue, otherValue)));
    }

    /**
     * join another stream, dynamic
     */
    @JExpExtensionMethod(name = "linqCrossJoin")
    @JExpFunction(lib = "linqimpl", name = "cross_join_dynamic")
    public static Stream<OrderedTuple> crossJoinDynamic(
        @This Stream<OrderedTuple> s, int nameIdx, JExpFunctionHandle otherSourceHandle) {
        return s.flatMap(thisValue ->
            StreamFunctions
                .withName(nameIdx, otherSourceHandle.invoke(thisValue.toArray()))
                .map(otherValue -> OrderedTuple.merge(thisValue, otherValue))
        );
    }
}