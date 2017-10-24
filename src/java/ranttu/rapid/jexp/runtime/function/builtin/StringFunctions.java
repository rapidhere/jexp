/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

/**
 * string function helpers
 *
 * @author rapidhere@gmail.com
 * @version $Id: StringFunctions.java, v0.1 2017-08-03 1:55 PM dongwei.dq Exp $
 */
public class StringFunctions {
    @JExpFunction(name = "string.is_blank")
    public static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    // TODO: @dongwei.dq, return int
    @JExpFunction(name = "string.length")
    public static Object length(String s) {
        return s.length();
    }
}
