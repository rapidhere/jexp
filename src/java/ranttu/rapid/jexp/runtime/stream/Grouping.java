/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * a grouping stream
 *
 * @author rapid
 * @version : Grouping.java, v 0.1 2020-11-13 4:47 PM rapid Exp $
 */
public class Grouping<K, E> extends DelegatedStream<Map.Entry<K, List<E>>> {
    private Grouping(Supplier<Stream<Map.Entry<K, List<E>>>> streamSupplier) {
        super(streamSupplier);
    }

    static <K0, E0> Grouping<K0, E0> ofGrouped(Supplier<Map<K0, List<E0>>> mapSupplier) {
        return new Grouping<>(() -> mapSupplier.get().entrySet().stream());
    }
}