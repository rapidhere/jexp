/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

import org.testng.annotations.BeforeClass;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.data.TestFunctions;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

/**
 * @author rapid
 * @version : ManualUnitTestBase.java, v 0.1 2020-11-05 11:28 AM rapid Exp $
 */
abstract public class ManualUnitTestBase {
    protected static CompileOption compileOption = new CompileOption();

    static {
        compileOption.debugInfo = true;
        compileOption.treatGetterNoSideEffect = true;
    }

    @BeforeClass
    public void registerTestFunctions() {
        try {
            JExpFunctionFactory.register(TestFunctions.class);
        } catch (Exception ignored) {
        }
    }
}