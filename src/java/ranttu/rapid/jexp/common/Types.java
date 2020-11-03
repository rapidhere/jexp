/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import lombok.experimental.UtilityClass;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

import java.util.List;

/**
 * Type utils
 *
 * @author dongwei.dq
 * @version $Id: Type.java, v0.1 2017-08-24 11:48 AM dongwei.dq Exp $
 */
@UtilityClass
public class Types {
    //~~~ common type constants
    public static Type JEXP_FLOAT = Type.DOUBLE_TYPE;

    public static Type JEXP_INT = Type.INT_TYPE;

    public static Type JEXP_STRING = Type.getType(String.class);

    public static Type JEXP_ARRAY = Type.getType(List.class);

    public static Type JEXP_GENERIC = Type.getType(Object.class);

    //~~~ helper functions
    public boolean isString(Type t) {
        return JEXP_STRING.equals(t);
    }

    public boolean isFloat(Type t) {
        return JEXP_FLOAT.equals(t);
    }

    public boolean isNumber(Type t) {
        return $.in(t.getSort(), Type.INT, Type.DOUBLE);
    }

    public boolean isInt(Type t) {
        return JEXP_INT.equals(t);
    }

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

    public Type getWrapper(Type t) {
        switch (t.getSort()) {
            case Type.INT:
                return Type.getType(Integer.class);
            case Type.BOOLEAN:
                return Type.getType(Boolean.class);
            case Type.BYTE:
                return Type.getType(Byte.class);
            case Type.SHORT:
                return Type.getType(Short.class);
            case Type.LONG:
                return Type.getType(Long.class);
            case Type.CHAR:
                return Type.getType(Character.class);
            case Type.FLOAT:
                return Type.getType(Float.class);
            case Type.DOUBLE:
                return Type.getType(Double.class);
            case Type.VOID:
                return Type.getType(Void.class);
            default:
                return null;
        }
    }

    public boolean isPrimitive(Class<?> c) {
        return isPrimitive(Type.getType(c));
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

    public boolean isType(Type t, Class<?> c) {
        return t.getClassName().equals(c.getName());
    }

    public Object getFrameDesc(Class<?> c) {
        Type t = Type.getType(c);
        switch (t.getSort()) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                return Opcodes.INTEGER;
            case Type.LONG:
                return Opcodes.LONG;
            case Type.FLOAT:
                return Opcodes.FLOAT;
            case Type.DOUBLE:
                return Opcodes.DOUBLE;
            default:
                return t.getInternalName();
        }
    }
}
