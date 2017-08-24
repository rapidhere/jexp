/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

/**
 * Type utils
 * @author dongwei.dq
 * @version $Id: Type.java, v0.1 2017-08-24 11:48 AM dongwei.dq Exp $
 */
final public class TypeUtil {
    private TypeUtil() {
    }

    public static boolean isString(Type t) {
        return !isPrimitive(t) && "java/lang/String".equals(t.getInternalName());
    }

    public static boolean isFloat(Type t) {
        return t.getSort() == Type.DOUBLE;
    }

    public static boolean isNumber(Type t) {
        return $.in(t.getSort(), Type.INT, Type.DOUBLE);
    }

    public static boolean isInt(Type t) {
        return t.getSort() == Type.INT;
    }

    public static Type getWrapper(Type t) {
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

    public static boolean isPrimitive(Class c) {
        return isPrimitive(Type.getType(c));
    }

    public static boolean isPrimitive(Type t) {
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

    public static Object getFrameDesc(Class c) {
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
