/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

import java.util.Date;

/**
 * @author rapid
 * @version $Id: CommonFunctions.java, v 0.1 2017年10月03日 3:41 PM rapid Exp $
 */
public class CommonFunctions {
    @JExpFunction(name = "sysdate")
    public static Date sysDate() {
        return new Date();
    }

    @JExpFunction(name = "test")
    public static Object test(Object test, Object trueHandle, Object falseHandle) {
        return JExpLang.exactBoolean(test) ? trueHandle : falseHandle;
    }
}