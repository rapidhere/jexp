/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.unit;

import lombok.experimental.var;
import org.testng.Assert;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.JExpExpression;
import ranttu.rapid.jexp.base.ManualUnitTestBase;
import ranttu.rapid.jexp.data.ImmutableRecursiveContext;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.exception.JExpFunctionArgumentConvertException;

import java.util.HashMap;

/**
 * @author rapid
 * @version : FuncExpTest.java, v 0.1 2020-11-03 10:50 PM rapid Exp $
 */
public class FuncExpTest extends ManualUnitTestBase {
    @Test
    public void testBinPrimitive() {
        try {
            JExp.compile("(a == 1) + b", compileOption);
            Assert.fail();
        } catch (JExpCompilingException e) {
            Assert.assertEquals(e.getMessage(), "PLUS, cannot apply type BOOL");
        }
    }

    @Test
    public void testBdInvokeExp() {
        try {
            var exp = JExp.compile("('a' + 'b').append('c')", compileOption);
            exp.exec(null);
            Assert.fail();
        } catch (NoSuchMethodError ignored) {
        }
    }

    @Test
    public void testUbBoolCE() {
        try {
            JExp.compile("int_ide(a == 1)", compileOption);
            Assert.fail();
        } catch (JExpFunctionArgumentConvertException ignored) {
        }

        var exp = JExp.compile("tf_ide(a == 1)", compileOption);
        var map = new HashMap<Object, Object>();
        map.put("a", 1);
        try {
            exp.exec(map);
            Assert.fail();
        } catch (ClassCastException ignored) {
        }
    }

    @Test
    public void testUbBoolWCE() {
        try {
            JExp.compile("int_ide('a' == 'a')", compileOption);
            Assert.fail();
        } catch (JExpFunctionArgumentConvertException ignored) {
        }

        var exp = JExp.compile("tf_ide('a' == 'a')", compileOption);
        try {
            exp.exec(null);
            Assert.fail();
        } catch (ClassCastException ignored) {
        }
    }

    @Test
    public void testUbIntWCE() {
        try {
            JExp.compile("bool_ide(1)", compileOption);
            Assert.fail();
        } catch (JExpFunctionArgumentConvertException ignored) {
        }

        var exp = JExp.compile("tf_ide(1)", compileOption);
        try {
            exp.exec(null);
            Assert.fail();
        } catch (ClassCastException ignored) {
        }
    }

    @Test
    public void testUbDoubleWCE() {
        try {
            JExp.compile("bool_ide(1.1)", compileOption);
            Assert.fail();
        } catch (JExpFunctionArgumentConvertException ignored) {
        }

        var exp = JExp.compile("tf_ide(1.1)", compileOption);
        try {
            exp.exec(null);
            Assert.fail();
        } catch (ClassCastException ignored) {
        }
    }

    @Test
    public void testUbGenericCE() {
        try {
            JExp.compile("byte_ide(1)", compileOption);
            Assert.fail();
        } catch (JExpFunctionArgumentConvertException ignored) {
        }

        var exp = JExp.compile("tf_ide(a)", compileOption);
        var map = new HashMap<Object, Object>();
        map.put("a", 1);
        try {
            exp.exec(map);
            Assert.fail();
        } catch (ClassCastException ignored) {
        }
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
    public void testListOrArrayAccessErr() {
        JExpExpression exp = JExp.compile("int_arr()['a']", compileOption);

        try {
            exp.execute(new Object());
            Assert.fail();
        } catch (ClassCastException ignored) {
        }

        exp = JExp.compile("[1, 2, 3]['a']", compileOption);
        try {
            exp.execute(new Object());
            Assert.fail();
        } catch (ClassCastException ignored) {
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