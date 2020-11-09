/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime;

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
            return !((Collection<?>) o).isEmpty();
        } else if (o instanceof Map) {
            return !((Map<?, ?>) o).isEmpty();
        } else {
            return o != null;
        }
    }

    // ~~~ result converts
    public boolean exactBoolean(Object o) {
        return booleanValue(o);
    }

    public byte exactByte(Object o) {
        return ((Number) o).byteValue();
    }

    public char exactCharacter(Object o) {
        return (Character) o;
    }

    public short exactShort(Object o) {
        return ((Number) o).shortValue();
    }

    public int exactInteger(Object o) {
        return ((Number) o).intValue();
    }

    public long exactLong(Object o) {
        return ((Number) o).longValue();
    }

    public float exactFloat(Object o) {
        return ((Number) o).floatValue();
    }

    public double exactDouble(Object o) {
        return ((Number) o).doubleValue();
    }
}