/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.unit;

import org.testng.Assert;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.JExpExpression;
import ranttu.rapid.jexp.base.ManualUnitTestBase;
import ranttu.rapid.jexp.data.ImmutableRecursiveContext;

/**
 * @author rapid
 * @version : FuncExpTest.java, v 0.1 2020-11-03 10:50 PM rapid Exp $
 */
public class FuncExpTest extends ManualUnitTestBase {
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