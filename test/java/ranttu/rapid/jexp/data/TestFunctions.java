/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.data;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.runtime.function.JExpFunction;

/**
 * @author rapid
 * @version $Id: TestFunctions.java, v 0.1 2017年10月25日 1:02 AM rapid Exp $
 */
public class TestFunctions {
    static int invokeTime = 0;

    @JExpFunction(name = "a")
    public static String a() {
        return "a";
    }

    @JExpFunction(name = "b")
    public static String b() {
        return "b";
    }

    @JExpFunction(name = "c")
    public static String c() {
        return "c";
    }

    @JExpFunction(name = "empty")
    public static String empty() {
        return "";
    }

    @JExpFunction(name = "one")
    public static int one() {
        return 1;
    }

    @JExpFunction(name = "two")
    public static int two() {
        return 2;
    }

    @JExpFunction(name = "sum")
    public static int sum(Object a, Object b, Object c) {
        return (Integer) a + (Integer) b + (Integer) c;
    }

    @JExpFunction(name = "get_str")
    public static String getStr(String a) {
        return "hello world: " + a + "!";
    }

    @JExpFunction(name = "int_arr")
    public static int[] intArr() {
        return new int[]{1, 2, 3, 4, 5};
    }

    @JExpFunction(name = "byte_arr")
    public static byte[] byteArr() {
        return new byte[]{1, 2, 3, 4, 5};
    }

    @JExpFunction(name = "short_arr")
    public static short[] shortArr() {
        return new short[]{1, 2, 3, 4, 5};
    }

    @JExpFunction(name = "long_arr")
    public static long[] longArr() {
        return new long[]{1, 2, 3, 4, 5};
    }

    @JExpFunction(name = "char_arr")
    public static char[] charArr() {
        return new char[]{1, 2, 3, 4, 5};
    }

    @JExpFunction(name = "float_arr")
    public static float[] floatArr() {
        return new float[]{(float) 1.1, (float) 2.2, (float) 3.3, (float) 4.4, (float) 5.5};
    }

    @JExpFunction(name = "double_arr")
    public static double[] doubleArr() {
        return new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
    }

    @JExpFunction(name = "boolean_arr")
    public static boolean[] booleanArr() {
        return new boolean[]{true, false};
    }

    @JExpFunction(name = "complex_arr")
    public static Object complexArr() {
        return new Object[][]{
            {true, false},
            {1, 2, 3, 4, 5},
            {1.1, 2.2, 3.3, 4.4, 5.5},
            {"s1", "s2", "s3", "s4", "s5"}
        };
    }

    @JExpFunction(name = "check_not_sb")
    public static void checkNotStringBuilder(Object o) {
        $.should(!(o instanceof StringBuilder));
    }

    @JExpFunction(name = "bool_ide")
    public static boolean boolIde(boolean b) {
        return b;
    }

    @JExpFunction(name = "boolw_ide")
    public static Boolean boolIde(Boolean b) {
        return b;
    }

    @JExpFunction(name = "int_ide")
    public static int intIde(int i) {
        return i;
    }

    @JExpFunction(name = "intw_ide")
    public static Integer intIde(Integer i) {
        return i;
    }

    @JExpFunction(name = "double_ide")
    public static double doubleIde(double d) {
        return d;
    }

    @JExpFunction(name = "byte_ide")
    public static byte byteIde(byte b) {
        return b;
    }

    @JExpFunction(name = "doublew_ide")
    public static Double doubleIde(Double d) {
        return d;
    }

    @JExpFunction(name = "tf_ide")
    public static TestFunctions tfIde(TestFunctions tf) {
        return tf;
    }

    @JExpFunction(name = "rand_arr")
    public static Object randArr() {
        invokeTime++;
        if (invokeTime == 1) {
            return new int[]{1, 2, 3, 4, 5};
        } else if (invokeTime == 2) {
            return new double[]{1.1, 1.2, 1.3, 1.4, 1.5};
        } else if (invokeTime == 3) {
            return new Object[]{"1", 2, 3.3, 4};
        } else {
            return new double[][]{{1.1, 1.2}, {1.3}, {1.4, 1.5}};
        }
    }
}