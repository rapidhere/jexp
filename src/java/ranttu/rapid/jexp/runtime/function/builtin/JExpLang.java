/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import ranttu.rapid.jexp.runtime.function.JExpFunction;

import java.util.Map;

/**
 * jexp basement function supports
 * @author dongwei.dq
 * @version $Id: JExpLang.java, v0.1 2017-08-31 8:45 PM dongwei.dq Exp $
 */
final public class JExpLang {
    // invoke
    @JExpFunction(name = "lang.invoke")
    public static Object invoke(Object o, String methodName, Object... args) throws Throwable {
        return MethodUtils.invokeMethod(o, methodName, args);
    }

    // getter
    @JExpFunction(name = "lang.get_prop")
    public static Object getProperty(Object o, String name) throws Throwable {
        if (o instanceof Map) {
            return ((Map) o).get(name);
        } else {
            return PropertyUtils.getProperty(o, name);
        }
    }

    @JExpFunction(name = "lang.equals")
    public static Object equals(Object a, Object b) {
        return a.equals(b);
    }

    // ~~~ math
    @JExpFunction(name = "math.add")
    public static Object add(Object a, Object b) {
        if (a instanceof String) {
            return new StringBuilder((String) a).append(b);
        } else if (a instanceof StringBuilder) {
            return ((StringBuilder) a).append(b);
        } else if (b instanceof String) {
            return new StringBuilder(String.valueOf(a)).append((String) b);
        } else if (b instanceof StringBuilder) {
            return ((StringBuilder) b).insert(0, a);
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
