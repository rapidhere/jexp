/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.unit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.JExpExpression;
import ranttu.rapid.jexp.base.JExpTestBase;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.data.ImmutableRecursiveContext;
import ranttu.rapid.jexp.data.TestFunctions;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

/**
 * @author rapid
 * @version : FuncExpTest.java, v 0.1 2020-11-03 10:50 PM rapid Exp $
 */
public class FuncExpTest extends JExpTestBase {
    static CompileOption compileOption = new CompileOption();

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

    @Test
    public void testArrAccess() {
        JExpExpression exp = JExp.compile("rand_arr()[1]", compileOption);

        Assert.assertEquals(2, exp.execute(null));
        Assert.assertEquals(1.2, exp.execute(null));
        Assert.assertEquals(2, exp.execute(null));
        Assert.assertEquals(new double[]{1.3}, exp.execute(null));

        JExpExpression exp2 = JExp.compile("rand_arr()[1][0]", compileOption);
        Assert.assertEquals(1.3, exp2.execute(0));
    }

    @Test
    public void testArrAccessMore() {
        Assert.assertEquals(Byte.valueOf("2"), JExp.eval("byte_arr()[1]", null));
        Assert.assertEquals(Short.valueOf("2"), JExp.eval("short_arr()[1]", null));
        Assert.assertEquals(Character.valueOf('\02'), JExp.eval("char_arr()[1]", null));
        Assert.assertEquals(Long.valueOf(2), JExp.eval("long_arr()[1]", null));
        Assert.assertEquals((float) 2.2, JExp.eval("float_arr()[1]", null));
    }

    @Test
    public void testArrOutOfIndex() {
        JExpExpression exp = JExp.compile("int_arr()[100]", compileOption);

        try {
            exp.execute(null);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            Assert.assertEquals(e.getMessage(), "100");
        }
    }

    @Test
    public void testListOutOfIndex() {
        JExpExpression exp = JExp.compile("[][100]", compileOption);

        try {
            exp.execute(null);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(e.getMessage(), "Index: 100, Size: 0");
        }
    }

    @Test
    public void testNoSuchField() {
        JExpExpression exp = JExp.compile("nima", compileOption);

        try {
            exp.execute(new Object());
            Assert.fail();
        } catch (NoSuchFieldError e) {
            Assert.assertEquals(e.getMessage(), "nima");
        }
    }

    @Test
    public void testNoSuchMethod() {
        JExpExpression exp = JExp.compile("a.nima()", compileOption);

        try {
            exp.execute(new ImmutableRecursiveContext());
            Assert.fail();
        } catch (NoSuchMethodError e) {
            Assert.assertEquals(e.getMessage(), "nima");
        }
    }
}