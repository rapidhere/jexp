/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.unit;

import lombok.experimental.var;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.JExpExpression;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.base.ManualUnitTestBase;
import ranttu.rapid.jexp.common.$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author rapid
 * @version : ManualUnitTest.java, v 0.1 2020-11-05 11:27 AM rapid Exp $
 */
public class ManualUnitTest extends ManualUnitTestBase {
    @Test
    public void testBoundedInvokeExtra() {
        JExpExpression exp = JExp.compile("a.checkNotSb('a' + 'b')", compileOption);
        var o = new Object() {
            @SuppressWarnings("unused")
            public void checkNotSb(Object o) {
                $.should(!(o instanceof StringBuilder));
            }

            @SuppressWarnings("unused")
            public boolean identity(boolean b) {
                return b;
            }
        };

        Map<Object, Object> m = new HashMap<>();
        m.put("a", o);
        m.put("b", "hello");
        m.put("c", 1);
        Assert.assertNull(exp.exec(m));

        exp = JExp.compile("a.checkNotSb(b + c)", compileOption);
        Assert.assertNull(exp.exec(m));

        exp = JExp.compile("a.identity(b == 'hello')", compileOption);
        Assert.assertTrue(exp.exec(m));
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
    public void testExecMethod() {
        var exp = JExp.compile("empty", compileOption);

        boolean res = exp.exec("'123'");
        Assert.assertFalse(res);
    }

    @Test
    public void testExecOnFunction() {
        var exp = JExp.compile("(a) => a", compileOption);

        JExpFunctionHandle res = exp.exec(null);

        Assert.assertFalse(res.exec(false));
    }

    @Test(enabled = false)
    public void testStream() {
        var exp = JExp.compile("a.stream().distinct().sorted((a, b) => a - b)", compileOption);
        var ctx = new HashMap<String, Object>() {
            {
                var l = new ArrayList<Integer>();
                l.add(5);
                l.add(3);
                l.add(4);
                l.add(2);
                l.add(2);
                l.add(5);
                l.add(1);
                put("a", l);
            }
        };

        Stream<Integer> stream = exp.exec(ctx);
        var res = stream.collect(Collectors.toList());
        Assert.assertEquals(res, Lists.newArrayList(1, 2, 3, 4, 5));

        exp = JExp.compile("a.stream().map((a) => a * 2)", compileOption);
        stream = exp.exec(ctx);
        res = stream.collect(Collectors.toList());
        Assert.assertEquals(res, Lists.newArrayList(10, 6, 8, 4, 4, 10, 2));
    }
}