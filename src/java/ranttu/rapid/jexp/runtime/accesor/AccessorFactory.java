/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

import ranttu.rapid.jexp.exception.JExpRuntimeException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.runtime.JExpClassLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_SUPER;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.ALOAD;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.CHECKCAST;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INSTANCEOF;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes.IRETURN;
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

        String className = "ranttu.rapid.jexp.JExpAccessor$" + accessorCount++;

        // start define
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        cw.visit(V1_6, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, className, null,
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
        mv = cw.visitMethod(ACC_PUBLIC, "get",
            getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, getInternalName(klass));

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

            onProperty(propertyName, m);
        }

        // end get method
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // generate bytecode
        cw.visitEnd();
        Class<Accessor> acKlass = JExpClassLoader.define(className, cw.toByteArray());

        try {
            return acKlass.newInstance();
        } catch (Throwable e) {
            throw new JExpRuntimeException("failed to generate accessor class!", e);
        }
    }

    private void onProperty(String name, Method method) {

    }
}