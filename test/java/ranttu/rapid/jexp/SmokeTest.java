/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp;

import lombok.experimental.var;
import org.testng.Assert;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.compile.CompileOption;
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

        CompileOption compileOption = new CompileOption();
        compileOption.debugInfo = true;
        compileOption.treatGetterNoSideEffect = true;

        var exp = JExp.compile("(a) => a", compileOption);

        JExpFunctionHandle res = exp.exec(null);

        Assert.assertFalse(res.exec(false));

//        JExpExpression exp = JExp.compile("(from i in a where i == 2 or i == 3 or i == 5 select i)", compileOption);
//        Stream<?> res = exp.exec(new HashMap<String, Object>() {
//            {
//                var l = new ArrayList<Integer>();
//                l.add(1);
//                l.add(2);
//                l.add(3);
//                l.add(4);
//                l.add(5);
//                put("a", l);
//                put("b", "abcde");
//                put("c", null);
//            }
//        });
//        System.out.println(res.collect(Collectors.toList()));
    }
}