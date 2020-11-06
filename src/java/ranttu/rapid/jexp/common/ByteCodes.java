/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import lombok.experimental.UtilityClass;
import lombok.experimental.var;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.Runtimes;

import java.util.HashMap;
import java.util.Map;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ALOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ARETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ASTORE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.DLOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.DRETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.DSTORE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.FLOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.FRETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.FSTORE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ILOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INVOKESTATIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.IRETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ISTORE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.LLOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.LRETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.LSTORE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.RETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * asm helper
 *
 * @author rapid
 * @version : ASMUtil.java, v 0.1 2020-11-06 4:39 PM rapid Exp $
 */
@UtilityClass
public class ByteCodes {
    /**
     * convert primitive types to wrapper type on need
     */
    public void box(MethodVisitor mv, Type type) {
        if (Types.isPrimitive(type)) {
            var wrapperClass = Types.getWrapperClass(type);
            $.should(wrapperClass != null);
            mv.visitMethodInsn(INVOKESTATIC, getInternalName(wrapperClass), "valueOf",
                "(" + type.getDescriptor() + ")" + getDescriptor(wrapperClass), false);
        }
    }

    /**
     * convert wrapper types to primitive type on need
     */
    public void unbox(MethodVisitor mv, Class<?> c) {
        // call Runtimes.exact* methods
        if (c.isPrimitive()) {
            var wrapperClass = Types.getWrapperClass(getType(c));
            $.should(wrapperClass != null);

            mv.visitMethodInsn(INVOKESTATIC,
                getInternalName(Runtimes.class),
                "exact" + wrapperClass.getSimpleName(),
                "(Ljava/lang/Object;)" + getDescriptor(c),
                false);
        }
    }

    /**
     * *LOAD
     */
    public void load(MethodVisitor mv, Class<?> type, int idx) {
        mv.visitVarInsn(matchOpc(type, OPS.LOAD), idx);
    }

    /**
     * *RETURN
     */
    public void ret(MethodVisitor mv, Class<?> type) {
        mv.visitInsn(matchOpc(type, OPS.RET));
    }

    //~~ impl

    /**
     * match the opcodes by type
     */
    private int matchOpc(Class<?> type, OPS op) {
        int opc;
        if (PRIMITIVE_OPCS.containsKey(type)) {
            opc = PRIMITIVE_OPCS.get(type)[op.ordinal()];
        } else {
            opc = PRIMITIVE_OPCS.get(Object.class)[op.ordinal()];
        }

        $.should(opc > 0);
        return opc;
    }

    /**
     * opcodes mapper
     */
    private final Map<Class<?>, int[]> PRIMITIVE_OPCS;

    static {
        // ~~~ primitive opcs
        PRIMITIVE_OPCS = new HashMap<>();
        PRIMITIVE_OPCS.put(int.class, opcs(ILOAD, ISTORE, IRETURN));
        PRIMITIVE_OPCS.put(boolean.class, opcs(ILOAD, ISTORE, IRETURN));
        PRIMITIVE_OPCS.put(byte.class, opcs(ILOAD, ISTORE, IRETURN));
        PRIMITIVE_OPCS.put(short.class, opcs(ILOAD, ISTORE, IRETURN));
        PRIMITIVE_OPCS.put(char.class, opcs(ILOAD, ISTORE, IRETURN));
        PRIMITIVE_OPCS.put(long.class, opcs(LLOAD, LSTORE, LRETURN));
        PRIMITIVE_OPCS.put(float.class, opcs(FLOAD, FSTORE, FRETURN));
        PRIMITIVE_OPCS.put(double.class, opcs(DLOAD, DSTORE, DRETURN));
        PRIMITIVE_OPCS.put(void.class, opcs(-1, -1, RETURN));
        PRIMITIVE_OPCS.put(Object.class, opcs(ALOAD, ASTORE, ARETURN));
    }

    private enum OPS {
        LOAD,
        STORE,
        RET,
    }

    private int[] opcs(int opLoad, int opStore, int opRet) {
        return new int[]{opLoad, opStore, opRet};
    }
}