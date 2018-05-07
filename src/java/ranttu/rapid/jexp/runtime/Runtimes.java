/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

/**
 * @author rapid
 * @version $Id: Runtimes.java, v 0.1 2018年04月24日 6:16 PM rapid Exp $
 */
@SuppressWarnings("unused")
@UtilityClass
public class Runtimes {
    /**
     * throw a no such method error
     */
    @SneakyThrows
    public Object noSuchMethod(String methodName, String className) {
        throw new NoSuchMethodException(className + "#" + methodName);
    }

    /**
     * calculate the boolean value of a object
     */
    public boolean booleanValue(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Number) {
            return ((Number) o).intValue() != 0;
        } else if (o instanceof String) {
            return ((String) o).length() != 0;
        } else if (o instanceof Collection) {
            return !((Collection) o).isEmpty();
        } else if (o instanceof Map) {
            return !((Map) o).isEmpty();
        } else {
            return o != null;
        }
    }
}