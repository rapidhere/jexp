/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import java.util.Date;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

/**
 * @author rapid
 * @version $Id: CommonFunctions.java, v 0.1 2017年10月03日 3:41 PM rapid Exp $
 */
public class CommonFunctions {
    @JExpFunction(name = "sysdate")
    public static String sysDate() {
        return new Date().toString();
    }
}