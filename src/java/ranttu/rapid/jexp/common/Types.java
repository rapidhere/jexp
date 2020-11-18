/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import lombok.experimental.UtilityClass;
import ranttu.rapid.jexp.compile.parse.ValueType;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

/**
 * Type utils
 *
 * @author dongwei.dq
 * @version $Id: Type.java, v0.1 2017-08-24 11:48 AM dongwei.dq Exp $
 */
@UtilityClass
public class Types {
    public boolean isString(ValueType vt) {
        return vt == ValueType.STRING || vt == ValueType.STRING_BUILDER;
    }

    public boolean isNumber(ValueType vt) {
        return vt == ValueType.DOUBLE_WRAPPED || vt == ValueType.INT_WRAPPED;
    }

    //~~~ helper functions
    public Type getPrimitive(Class<?> c) {
        if (c == Integer.class) {
            return Type.INT_TYPE;
        } else if (c == Boolean.class) {
            return Type.BOOLEAN_TYPE;
        } else if (c == Byte.class) {
            return Type.BYTE_TYPE;
        } else if (c == Short.class) {
            return Type.SHORT_TYPE;
        } else if (c == Long.class) {
            return Type.LONG_TYPE;
        } else if (c == Character.class) {
            return Type.CHAR_TYPE;
        } else if (c == Float.class) {
            return Type.FLOAT_TYPE;
        } else if (c == Double.class) {
            return Type.DOUBLE_TYPE;
        } else if (c == Void.class) {
            return Type.VOID_TYPE;
        } else {
            return null;
        }
    }

    public Class<?> getWrapperClass(Type t) {
        switch (t.getSort()) {
            case Type.INT:
                return Integer.class;
            case Type.BOOLEAN:
                return Boolean.class;
            case Type.BYTE:
                return Byte.class;
            case Type.SHORT:
                return Short.class;
            case Type.LONG:
                return Long.class;
            case Type.CHAR:
                return Character.class;
            case Type.FLOAT:
                return Float.class;
            case Type.DOUBLE:
                return Double.class;
            case Type.VOID:
                return Void.class;
            default:
                return null;
        }
    }

    public boolean isPrimitive(Type t) {
        switch (t.getSort()) {
            case Type.INT:
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.SHORT:
            case Type.LONG:
            case Type.CHAR:
            case Type.FLOAT:
            case Type.DOUBLE:
            case Type.VOID:
                return true;
            default:
                return false;
        }
    }
}
