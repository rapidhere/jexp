/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.data;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

/**
 * @author rapid
 * @version $Id: TestFunctions.java, v 0.1 2017年10月25日 1:02 AM rapid Exp $
 */
public class TestFunctions {
    @JExpFunction(name = "a")
    public static String a() {
        return "a";
    }

    @JExpFunction(name = "b")
    public static String b() {
        return "b";
    }

    @JExpFunction(name = "c")
    public static String c() {
        return "c";
    }

    @JExpFunction(name = "empty")
    public static String empty() {
        return "";
    }

    @JExpFunction(name = "one")
    public static Integer one() {
        return 1;
    }

    @JExpFunction(name = "two")
    public static Integer two() {
        return 2;
    }

    @JExpFunction(name = "sum")
    public static Integer sum(Object a, Object b, Object c) {
        return (Integer) a + (Integer) b + (Integer) c;
    }

    @JExpFunction(name = "get_str")
    public static String getStr(String a) {
        return "hello world: " + a + "!";
    }
}