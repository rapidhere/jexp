/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp;

import lombok.experimental.var;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.data.TestFunctions;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author rapid
 * @version : SmokeTest.java, v 0.1 2020-11-01 5:18 PM rapid Exp $
 */
public class SmokeTest {
    @Test(enabled = false)
    public void test0() {
        try {
            JExpFunctionFactory.register(TestFunctions.class);
        } catch (Throwable ignored) {
        }

        CompileOption compileOption = new CompileOption();
        compileOption.debugInfo = true;
        compileOption.treatGetterNoSideEffect = true;


        JExpExpression exp = JExp.compile("(from w in a orderby w.length() descending, w ascending select w).toList()", compileOption);
        List<?> res = exp.exec(new HashMap<String, Object>() {
            {
                var l = new ArrayList<String>();
                l.add("hello");
                l.add("world");
                l.add("peaches");
                l.add("pears");
                l.add("oranges");
                l.add("hat");
                l.add("bug");

                put("a", l);
            }
        });
        System.out.println(res);
    }
}