/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

import java.util.Date;

/**
 * string function helpers
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpStringFunction.java, v0.1 2017-08-03 1:55 PM dongwei.dq Exp $
 */
public class JExpStringFunction {
    @JExpFunction(name = "is_blank")
    public static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    @JExpFunction(name = "length3")
    public static int length(String s) {
        return s.length() + s.length() + s.length();
    }

    @JExpFunction(name = "sysdate")
    public static String sysDate() {
        return new Date().toString();
    }
}
