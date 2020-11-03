/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp;

import org.testng.Assert;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.JExpExpression;
import ranttu.rapid.jexp.data.ImmutableRecursiveContext;
import ranttu.rapid.jexp.data.TestFunctions;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

/**
 * @author rapid
 * @version : SmokeTest.java, v 0.1 2020-11-01 5:18 PM rapid Exp $
 */
public class SmokeTest {
    @Test()
    public void test0() {
        JExpFunctionFactory.register(TestFunctions.class);
        Object o = new ImmutableRecursiveContext();

        CompileOption compileOption = new CompileOption();
        compileOption.debugInfo = true;
        compileOption.treatGetterNoSideEffect = true;

        JExpExpression exp = JExp.compile("[1, 2].a()", compileOption);
        Assert.assertEquals(2, exp.execute(o));
    }
}