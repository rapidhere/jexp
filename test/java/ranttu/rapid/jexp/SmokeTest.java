/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp;

import org.testng.annotations.Test;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.data.ImmutableRecursiveContext;
import ranttu.rapid.jexp.data.TestFunctions;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

/**
 * @author rapid
 * @version : SmokeTest.java, v 0.1 2020-11-01 5:18 PM rapid Exp $
 */
public class SmokeTest {
    @Test(enabled = true)
    public void test0() {
        try {
            JExpFunctionFactory.register(TestFunctions.class);
        } catch (Throwable ignored) {
        }

        Object o = new ImmutableRecursiveContext();

        CompileOption compileOption = new CompileOption();
        compileOption.debugInfo = true;
        compileOption.treatGetterNoSideEffect = true;

        JExpExpression exp = JExp.compile("(b) => { (a) => {a + b + o.o.c} }", compileOption);
        JExpFunctionHandle funcOuter = exp.exec(o);
        JExpFunctionHandle funcInner = funcOuter.exec(2);
        System.out.println((Object) funcInner.exec(1));
    }
}