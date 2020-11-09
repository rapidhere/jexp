/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp;

import lombok.experimental.var;

import java.util.concurrent.Callable;

/**
 * a jexp function handle
 *
 * @author rapid
 * @version : JExpFunction.java, v 0.1 2020-11-05 11:22 AM rapid Exp $
 */
@FunctionalInterface
public interface JExpFunctionHandle
    /**
     * impl for common functional interfaces
     * to avoid runtime compiling
     *
     * NOTE:
     * functional interfaces under java.util.function has many default methods,
     * may produce method conflicts when generate compiling adaptors,
     * so won't impl by default
     */
    extends
    /**
     * package: java.lang;
     */
    Runnable,

    /**
     * package: java.util.concurrent;
     */
    Callable<Object> {

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
     * @see JExpFunctionHandle#invoke(Object[])
     */
    default <T> T exec(Object... args) {
        @SuppressWarnings("unchecked") var res = (T) invoke(args);
        return res;
    }

    //~~~ impl for common functional interface

    /**
     * @see Callable#call()
     */
    @Override
    default Object call() {
        return invoke(new Object[0]);
    }

    /**
     * @see Runnable#run()
     */
    @Override
    default void run() {
        invoke(new Object[0]);
    }
}