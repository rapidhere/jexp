/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime;

import lombok.experimental.var;
import lombok.val;
import ranttu.rapid.jexp.JExpFunctionHandle;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.ByteCodes;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.AASTORE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_FINAL;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_SUPER;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ALOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ANEWARRAY;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.DUP;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.GETFIELD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.POP;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.PUTFIELD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.RETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.V1_7;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * runtime compile management
 *
 * @author rapid
 * @version : RuntimeCompiling.java, v 0.1 2020-11-06 3:00 PM rapid Exp $
 */
final public class RuntimeCompiling {
    private RuntimeCompiling() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * the name count
     */
    private static AtomicLong nameCountPool = new AtomicLong(0);

    /**
     * define a temporary class
     */
    public static <T> Class<T> defineTemporaryClass(String className, byte[] bytes) {
        // for debug
        $.printClass(className, bytes);

        try {
            return JExpClassLoader.define(className, bytes);
        } catch (Exception e) {
            throw new JExpCompilingException("error when define compiled class", e);
        }
    }

    /**
     * next jexp expression name
     */
    public static String nextExpressionName() {
        return "ranttu.rapid.jexp.JExpCompiledExpression$" + nameCountPool.getAndIncrement();
    }

    /**
     * next jexp function name
     */
    public static String nextFunctionName() {
        return "ranttu.rapid.jexp.JExpCompiledFunction$" + nameCountPool.getAndIncrement();
    }

    /**
     * next sam glue name
     */
    public static String nextSAMAdaptorName() {
        return "ranttu.rapid.jexp.JExpCompiledSAMAdaptor$" + nameCountPool.getAndIncrement();
    }

    //~~~ SAM Adaptor generator

    /**
     * generate a sam adaptor class
     */
    public static <T> Class<T> genSAMAdaptorClass(Class<T> target, Method samMethod) {
        var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        var className = nextSAMAdaptorName();
        var classInternalName = className.replace('.', '/');
        val delegatorName = "$delegate";

        Class<?> superClass = target.isInterface() ? Object.class : target;

        //~~~ class prepare
        cw.visit(V1_7, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, classInternalName, null,
            getInternalName(superClass),
            target.isInterface() ? new String[]{getInternalName(target)} : null);
        cw.visitSource("<jexp-sam-adaptor>",
            "adapt to " + target.getName() + "#" + samMethod.getName());

        // create field
        cw.visitField(ACC_SYNTHETIC + ACC_FINAL + ACC_PUBLIC, delegatorName,
            getDescriptor(JExpFunctionHandle.class), null, null);

        //~~~ constructor
        var conMv = cw.visitMethod(ACC_PUBLIC, "<init>",
            getMethodDescriptor(Type.VOID_TYPE, getType(JExpFunctionHandle.class)),
            null, null);
        conMv.visitCode();
        conMv.visitVarInsn(ALOAD, 0);
        conMv.visitMethodInsn(INVOKESPECIAL, getInternalName(superClass), "<init>", "()V",
            false);
        // set delegator
        conMv.visitVarInsn(ALOAD, 0);
        conMv.visitVarInsn(ALOAD, 1);
        conMv.visitFieldInsn(PUTFIELD, classInternalName,
            delegatorName, getDescriptor(JExpFunctionHandle.class));

        // end of constructor
        conMv.visitInsn(RETURN);
        conMv.visitMaxs(0, 0);
        conMv.visitEnd();

        //~~~ impl of the sam method
        var mv = cw.visitMethod(ACC_PUBLIC, samMethod.getName(),
            getMethodDescriptor(samMethod), null, null);

        // put jexp function on stack
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classInternalName,
            delegatorName, getDescriptor(JExpFunctionHandle.class));

        // create invoke array
        mv.visitLdcInsn(samMethod.getParameterCount());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        var index = 0;
        for (var parType : samMethod.getParameterTypes()) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(index);
            ByteCodes.load(mv, parType, index + 1);
            ByteCodes.box(mv, getType(parType));
            mv.visitInsn(AASTORE);

            index++;
        }

        // call jexp function
        mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(JExpFunctionHandle.class), "invoke",
            "([Ljava/lang/Object;)Ljava/lang/Object;", true);

        // handle invoke result
        // drop on void return
        if (samMethod.getReturnType() == void.class) {
            mv.visitInsn(POP);
        }
        // convert to specified type
        else {
            ByteCodes.unbox(mv, samMethod.getReturnType());
        }

        // return
        ByteCodes.ret(mv, samMethod.getReturnType());

        // end of adaptor method
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        //~~~ define class
        return defineTemporaryClass(className, cw.toByteArray());
    }
}