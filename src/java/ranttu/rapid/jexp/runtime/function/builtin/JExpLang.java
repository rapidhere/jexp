/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * jexp basement function supports
 * @author dongwei.dq
 * @version $Id: JExpLang.java, v0.1 2017-08-31 8:45 PM dongwei.dq Exp $
 */
final public class JExpLang {
    // getter
    @JExpFunction(name = "lang.get_prop")
    public static Object getProperty(Object o, String name) throws Throwable {
        if (o instanceof Map) {
            return ((Map) o).get(name);
        } else {
            Field f = o.getClass().getField(name);
            f.setAccessible(true);
            return f.get(o);
        }
    }

    // ~~~ math
    @JExpFunction(name = "math.add")
    public static Object add(Object a, Object b) {
        if (a instanceof String || b instanceof String) {
            return String.valueOf(a) + String.valueOf(b);
        } else {
            Number numA = (Number) a, numB = (Number) b;

            if (a instanceof Double || b instanceof Double) {
                return numA.doubleValue() + numB.doubleValue();
            } else {
                return numA.intValue() + numB.intValue();
            }
        }
    }

    @JExpFunction(name = "math.sub")
    public static Object sub(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() - numB.doubleValue();
        } else {
            return numA.intValue() - numB.intValue();
        }
    }

    @JExpFunction(name = "math.mul")
    public static Object mul(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() * numB.doubleValue();
        } else {
            return numA.intValue() * numB.intValue();
        }
    }

    @JExpFunction(name = "math.div")
    public static Object div(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() / numB.doubleValue();
        } else {
            return numA.intValue() / numB.intValue();
        }
    }

    @JExpFunction(name = "math.mod")
    public static Object mod(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() % numB.doubleValue();
        } else {
            return numA.intValue() % numB.intValue();
        }
    }
}