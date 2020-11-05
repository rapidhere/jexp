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

/**
 * @author rapid
 * @version : ManualUnitTest.java, v 0.1 2020-11-05 11:27 AM rapid Exp $
 */
public class ManualUnitTest extends ManualUnitTestBase {
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
    public void testExecMethod() {
        JExpExpression exp = JExp.compile("empty", compileOption);

        boolean res = exp.exec("'123'");
        Assert.assertFalse(res);
    }
}