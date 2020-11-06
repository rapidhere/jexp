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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        JExpExpression exp = JExp.compile("a.stream().map((a) => a * 2)", compileOption);
        Stream<Integer> stream = exp.exec(new HashMap<String, Object>() {
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
        });
        System.out.println(stream.collect(Collectors.toList()));
    }
}