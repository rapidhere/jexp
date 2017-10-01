/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.unit;

import org.testng.annotations.BeforeClass;
import ranttu.rapid.jexp.base.JExpUnitTestBase;
import ranttu.rapid.jexp.runtime.function.JExpFunction;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

/**
 * @author dongwei.dq
 * @version $Id: PrimaryExpression.java, v0.1 2017-07-28 7:12 PM dongwei.dq Exp $
 */
public class FunctionExpression extends JExpUnitTestBase {
    @BeforeClass
    public void prepareFunctions() {
        JExpFunctionFactory.register(TestFunctions.class);
    }
}

@SuppressWarnings("unused")
class TestFunctions {
    @JExpFunction(name = "one")
    public static int one() {
        return 1;
    }

    @JExpFunction(name = "two")
    public static int two() {
        return 2;
    }

    @JExpFunction(name = "sum")
    public static int sum(int a, int b, int c) {
        return a + b + c;
    }

    @JExpFunction(name = "get_str")
    public static String getStr(String a) {
        return "hello world: " + a + "!";
    }
}
