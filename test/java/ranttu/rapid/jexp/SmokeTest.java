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
    @Test()
    public void test0() {
        try {
            JExpFunctionFactory.register(TestFunctions.class);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }

        CompileOption compileOption = new CompileOption();
        compileOption.debugInfo = true;
        compileOption.treatGetterNoSideEffect = true;

        JExpExpression exp = JExp.compile("a.stream().map((a) => a * 2).toList()", compileOption);
        List<?> res = exp.exec(new HashMap<String, Object>() {
            {
                var l = new ArrayList<Integer>();
                l.add(1);
                l.add(2);
                l.add(3);
                l.add(4);
                l.add(5);
                put("a", l);
                put("b", "abcde");
            }
        });
        System.out.println(res);
    }
}