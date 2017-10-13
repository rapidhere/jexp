/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.exception.JExpRuntimeException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.runtime.JExpClassLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_SUPER;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ALOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ARETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ATHROW;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.CHECKCAST;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.DUP;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.F_SAME;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.IFEQ;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INSTANCEOF;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.IRETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.NEW;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.RETURN;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.V1_6;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * the accessor generate factorys
 *
 * @author rapid
 * @version $Id: AccessorFactory.java, v 0.1 2017年10月03日 4:40 PM rapid Exp $
 */
final public class AccessorFactory {
    private AccessorFactory() {
    }

    @SuppressWarnings("unused")
    public static Accessor getAccessor(Object o) {
        return theFactory.get(o.getClass());
    }

    //~~~ impl

    private static final AccessorFactory theFactory    = new AccessorFactory();

    private Map<Class, Accessor>         accessorStore = new WeakHashMap<>();

    private static int                   accessorCount = 0;

    private Accessor get(Class c) {
        return accessorStore.computeIfAbsent(c, this::generateAccessor);
    }

    private Accessor generateAccessor(Class klass) {
        // use klass name, so they have same package access privilege
        String className = getAccessorName(klass);
        String classInternalName = className.replace(".", "/");

        // start define
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        cw.visit(V1_6, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, classInternalName, null,
            getInternalName(Object.class), new String[] { getInternalName(Accessor.class) });
        cw.visitSource("<jexp-accessor>", null);

        // constructor method
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // isSatisfied method
        mv = cw.visitMethod(ACC_PUBLIC, "isSatisfied",
            getMethodDescriptor(getType(boolean.class), getType(Object.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, getInternalName(klass));
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // get method
        mv = cw
            .visitMethod(
                ACC_PUBLIC,
                "get",
                getMethodDescriptor(getType(Object.class), getType(Object.class),
                    getType(String.class)), null, null);
        mv.visitCode();

        // get accessors
        Map<String, Method> accessorMethods = collectAccessorMethod(klass);
        Map<Integer, List<String>> accessorHashCodes = calculateHashCode(accessorMethods);

        // prepare hashCodes
        int[] hashCodes = getHashCodesArray(accessorHashCodes.keySet());

        // prepare labels
        Label[] switchLabels = new Label[accessorHashCodes.size()];
        for (int i = 0; i < switchLabels.length; i++) {
            switchLabels[i] = new Label();
        }
        Label defaultLabel = new Label();

        // switch-case string hash
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
        mv.visitLookupSwitchInsn(defaultLabel, hashCodes, switchLabels);

        // start case visit
        for (int i = 0; i < switchLabels.length; i++) {
            int hashCode = hashCodes[i];
            Label label = switchLabels[i];

            mv.visitLabel(label);
            mv.visitFrame(F_SAME, 0, null, 0, null);

            // current if label
            Label currentEqualLabel = null;

            // if-else chain
            List<String> propertyNames = accessorHashCodes.get(hashCode);
            for (int j = 0; j < propertyNames.size(); j++) {
                String propertyName = propertyNames.get(j);

                if (currentEqualLabel != null) {
                    mv.visitLabel(currentEqualLabel);
                    mv.visitFrame(F_SAME, 0, null, 0, null);
                    continue;
                }

                // if name equals ?
                mv.visitLdcInsn(propertyName);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Object.class), "equals",
                    getMethodDescriptor(getType(boolean.class), getType(Object.class)), false);

                // for last property, just goto default(failed) label
                if (j == propertyNames.size() - 1) {
                    mv.visitJumpInsn(IFEQ, defaultLabel);
                }
                // else, jump to next equal label
                else {
                    currentEqualLabel = new Label();
                    mv.visitJumpInsn(IFEQ, currentEqualLabel);
                }

                // current access statement
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, getInternalName(klass));
                Method am = accessorMethods.get(propertyName);
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(klass), am.getName(),
                    getMethodDescriptor(am), false);
                mv.visitInsn(ARETURN);
            }
        }

        // visit default label, throw new NoSuchFieldError
        mv.visitLabel(defaultLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);

        mv.visitTypeInsn(NEW, getInternalName(NoSuchFieldError.class));
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, getInternalName(NoSuchFieldError.class), "<init>",
            "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);

        // end get method
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // generate bytecode
        cw.visitEnd();

        byte[] bc = cw.toByteArray();
        $.printClass(className, bc);
        Class<Accessor> acKlass = JExpClassLoader.define(className, bc);

        try {
            return acKlass.newInstance();
        } catch (Throwable e) {
            throw new JExpRuntimeException("failed to generate accessor class!", e);
        }
    }

    private Map<String, Method> collectAccessorMethod(Class klass) {
        Map<String, Method> res = new HashMap<>();

        for (Method m : klass.getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            if (m.getParameterCount() > 0) {
                continue;
            }

            String propertyName;
            if (m.getName().startsWith("get")) {
                propertyName = m.getName().substring(3);
            } else if (m.getName().startsWith("is")) {
                propertyName = m.getName().substring(2);
            } else {
                continue;
            }
            propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);

            res.put(propertyName, m);
        }

        return res;
    }

    private String getAccessorName(Class klass) {
        return klass.getName() + "$Accessor$" + accessorCount++;
    }

    private Map<Integer, List<String>> calculateHashCode(Map<String, Method> accessorMethods) {
        Map<Integer, List<String>> res = new HashMap<>();

        for (String propertyName : accessorMethods.keySet()) {
            res.putIfAbsent(propertyName.hashCode(), new ArrayList<>());
            res.get(propertyName.hashCode()).add(propertyName);
        }

        return res;
    }

    private int[] getHashCodesArray(Set<Integer> hashCodeSet) {
        int res[] = new int[hashCodeSet.size()];
        int i = 0;
        for (int hash : hashCodeSet) {
            res[i] = hash;
            i++;
        }

        return res;
    }
}