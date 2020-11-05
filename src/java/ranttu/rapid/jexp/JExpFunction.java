/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp;

import lombok.experimental.var;

/**
 * a jexp function handle
 *
 * @author rapid
 * @version : JExpFunction.java, v 0.1 2020-11-05 11:22 AM rapid Exp $
 */
@FunctionalInterface
public interface JExpFunction {
    /**
     * invoke the function
     *
     * @param args arguments to pass
     * @return exec result
     */
    Object invoke(Object[] args);

    //~~~ helper methods

    /**
     * a convenience for default invoke method
     *
     * @see JExpFunction#invoke(Object[])
     */
    default <T> T exec(Object... args) {
        @SuppressWarnings("unchecked") var res = (T) invoke(args);
        return res;
    }
}