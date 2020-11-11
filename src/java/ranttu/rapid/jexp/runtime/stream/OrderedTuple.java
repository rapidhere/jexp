/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.compile.CompileOption;

/**
 * a tuple combined names
 *
 * @author rapid
 * @version : JExpTuple.java, v 0.1 2020-11-10 9:16 PM rapid Exp $
 */
class OrderedTuple {
    private final Object[] values;

    private OrderedTuple(Object[] values) {
        this.values = values;
    }

    Object[] toArray() {
        return values;
    }

    OrderedTuple put(int idx, Object val) {
        var res = new OrderedTuple(values.clone());
        $.should(res.values[idx] == null);
        res.values[idx] = val;

        return res;
    }

    Object get(int idx) {
        return values[idx];
    }

    static OrderedTuple of(int idx, Object val) {
        var tuple = new OrderedTuple(new Object[CompileOption.MAX_LINQ_PARS]);
        tuple.values[idx] = val;

        return tuple;
    }

    static OrderedTuple merge(OrderedTuple a, OrderedTuple b) {
        var res = new OrderedTuple(a.values.clone());
        for (int i = 0; i < CompileOption.MAX_LINQ_PARS; i++) {
            if (b.values[i] != null) {
                $.should(res.values[i] == null);
                res.values[i] = b.values[i];
            }
        }

        return res;
    }
}