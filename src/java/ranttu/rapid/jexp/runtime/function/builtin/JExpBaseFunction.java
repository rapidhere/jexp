/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import ranttu.rapid.jexp.runtime.function.JExpFunction;

import java.util.Map;

/**
 * jexp basement function supports
 * @author dongwei.dq
 * @version $Id: JExpBaseFunction.java, v0.1 2017-08-31 8:45 PM dongwei.dq Exp $
 */
@SuppressWarnings("unused")
final public class JExpBaseFunction {
    // getter
    @JExpFunction(name = "get_prop")
    public static Object getProperty(Object o, String name) {
        if (o instanceof Map) {
            return ((Map) o).get(name);
        } else {
            // not supported yet
            return null;
        }
    }

    // ~~~ math
    @JExpFunction
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

    @JExpFunction
    public static Object sub(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() - numB.doubleValue();
        } else {
            return numA.intValue() - numB.intValue();
        }
    }

    @JExpFunction
    public static Object mul(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() * numB.doubleValue();
        } else {
            return numA.intValue() * numB.intValue();
        }
    }

    @JExpFunction
    public static Object div(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() / numB.doubleValue();
        } else {
            return numA.intValue() / numB.intValue();
        }
    }

    @JExpFunction
    public static Object mod(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() % numB.doubleValue();
        } else {
            return numA.intValue() % numB.intValue();
        }
    }
}
