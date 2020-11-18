/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function.builtin;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.runtime.function.JExpFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * jexp basement function supports
 *
 * @author dongwei.dq
 * @version $Id: JExpLang.java, v0.1 2017-08-31 8:45 PM dongwei.dq Exp $
 */
final public class JExpLang {
    //~~~ invokers
    @JExpFunction(lib = "lang", name = "invoke")
    public static Object invoke(Object o, String methodName, Object... args) throws Throwable {
        return MethodUtils.invokeMethod(o, methodName, args);
    }

    @JExpFunction(lib = "lang", name = "invoke_no_args")
    public static Object invoke(Object o, String methodName) throws Throwable {
        return MethodUtils.invokeMethod(o, methodName, new Object[]{});
    }

    @JExpFunction(lib = "lang", name = "run_func")
    public static Object runJExpFunction(JExpFunctionHandle functionHandle, List<Object> args) {
        return functionHandle.invoke(args.toArray());
    }

    @JExpFunction(lib = "lang", name = "get_prop")
    public static Object getProperty(Object o, String name) throws Throwable {
        if (o instanceof Map) {
            return ((Map<?, ?>) o).get(name);
        } else {
            return PropertyUtils.getProperty(o, name);
        }
    }

    @JExpFunction(lib = "lang", name = "sb_to_str")
    public static Object stringBuilderToString(Object o) {
        return (o instanceof StringBuilder) ? o.toString() : o;
    }

    // ~~~ result converts
    @JExpFunction(lib = "lang", name = "bool")
    public static boolean exactBoolean(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Number) {
            return ((Number) o).intValue() != 0;
        } else if (o instanceof String) {
            return ((String) o).length() != 0;
        } else if (o instanceof Collection) {
            return !((Collection<?>) o).isEmpty();
        } else if (o instanceof Map) {
            return !((Map<?, ?>) o).isEmpty();
        } else {
            return o != null;
        }
    }

    @JExpFunction(lib = "lang", name = "eq")
    public static boolean eq(Object o, Object b) {
        if (o == null) {
            return b == null;
        } else {
            return o.equals(b);
        }
    }

    @JExpFunction(lib = "lang", name = "neq")
    public static boolean notEq(Object o, Object b) {
        return !eq(o, b);
    }

    @JExpFunction(lib = "lang", name = "byte")
    public static byte exactByte(Object o) {
        return ((Number) o).byteValue();
    }

    @JExpFunction(lib = "lang", name = "char")
    public static char exactCharacter(Object o) {
        return (Character) o;
    }

    @JExpFunction(lib = "lang", name = "short")
    public static short exactShort(Object o) {
        return ((Number) o).shortValue();
    }

    @JExpFunction(lib = "lang", name = "int")
    public static int exactInteger(Object o) {
        return ((Number) o).intValue();
    }

    @JExpFunction(lib = "lang", name = "long")
    public static long exactLong(Object o) {
        return ((Number) o).longValue();
    }

    @JExpFunction(lib = "lang", name = "float")
    public static float exactFloat(Object o) {
        return ((Number) o).floatValue();
    }

    @JExpFunction(lib = "lang", name = "double")
    public static double exactDouble(Object o) {
        return ((Number) o).doubleValue();
    }

    //~~~ maths
    @JExpFunction(lib = "math", name = "minus")
    public static Object minus(Object a) {
        Number number = (Number) a;

        if (a instanceof Double) {
            return -number.doubleValue();
        } else {
            return -number.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "not")
    public static boolean not(Object a) {
        return !exactBoolean(a);
    }

    @JExpFunction(lib = "math", name = "bnot")
    public static boolean not(boolean a) {
        return !a;
    }

    @JExpFunction(lib = "math", name = "grt")
    public static boolean greater(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() > numB.doubleValue();
        } else {
            return numA.intValue() > numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "gre")
    public static boolean greaterEq(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() >= numB.doubleValue();
        } else {
            return numA.intValue() >= numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "lst")
    public static boolean smaller(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() < numB.doubleValue();
        } else {
            return numA.intValue() < numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "lse")
    public static boolean smallerEq(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() <= numB.doubleValue();
        } else {
            return numA.intValue() <= numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "add")
    public static Object add(Object a, Object b) {
        if (a instanceof String) {
            return new StringBuilder((String) a).append(b);
        } else if (a instanceof StringBuilder) {
            return ((StringBuilder) a).append(b);
        } else if (b instanceof String) {
            return new StringBuilder(String.valueOf(a)).append((String) b);
        } else if (b instanceof StringBuilder) {
            return ((StringBuilder) b).insert(0, a);
        } else if (a instanceof Character || b instanceof Character) {
            return new StringBuilder().append(a).append(b);
        } else {
            Number numA = (Number) a, numB = (Number) b;

            if (a instanceof Double || b instanceof Double) {
                return numA.doubleValue() + numB.doubleValue();
            } else {
                return numA.intValue() + numB.intValue();
            }
        }
    }

    @JExpFunction(lib = "math", name = "sub")
    public static Object sub(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() - numB.doubleValue();
        } else {
            return numA.intValue() - numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "mul")
    public static Object mul(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() * numB.doubleValue();
        } else {
            return numA.intValue() * numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "div")
    public static Object div(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() / numB.doubleValue();
        } else {
            return numA.intValue() / numB.intValue();
        }
    }

    @JExpFunction(lib = "math", name = "mod")
    public static Object mod(Object a, Object b) {
        Number numA = (Number) a, numB = (Number) b;

        if (a instanceof Double || b instanceof Double) {
            return numA.doubleValue() % numB.doubleValue();
        } else {
            return numA.intValue() % numB.intValue();
        }
    }
}
