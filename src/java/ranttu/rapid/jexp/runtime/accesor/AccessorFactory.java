/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.exception.JExpRuntimeException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.runtime.JExpClassLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * the accessor generate factorys
 *
 * @author rapid
 * @version $Id: AccessorFactory.java, v 0.1 2017年10月03日 4:40 PM rapid Exp $
 */
final public class AccessorFactory implements Opcodes {
    private AccessorFactory() {
    }

    @SuppressWarnings("unused")
    public static Accessor getAccessor(Object o, String key) {
        return theFactory.get(o, key);
    }

    //~~~ impl

    private static final AccessorFactory    theFactory          = new AccessorFactory();

    private Map<String, Accessor>           accessorStore       = new HashMap<>();

    private Map<Class, Map<String, Method>> accessorMethodCache = new WeakHashMap<>();

    private static int                      accessorCount       = 0;

    private Accessor get(Object o, String key) {
        if (o == null) {
            return null;
        }

        Class klass = o.getClass();
        String storedKey = klass.getName() + "#" + key;

        return accessorStore.computeIfAbsent(storedKey, s -> {
            if (o instanceof Map) {
                return MapAccessor.of(key);
            } else {
                return generateAccessor(klass, key);
            }
        });
    }

    private Accessor generateAccessor(Class klass, String key) {
        // get accessors
        Method accessorMethod = getAccessorMethod(klass, key);
        if (accessorMethod == null) {
            return DummyAccessor.ACCESSOR;
        }

        // use klass name, so they have same package access privilege
        String className = getAccessorName(klass, key);
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
        mv = cw.visitMethod(ACC_PUBLIC, "get",
            getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, getInternalName(klass));
        mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(klass), accessorMethod.getName(),
            getMethodDescriptor(accessorMethod), false);
        mv.visitInsn(ARETURN);

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

    private Method getAccessorMethod(Class klass, String key) {
        return accessorMethodCache.computeIfAbsent(klass, this::collectAccessorMethod).get(key);
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

    private String getAccessorName(Class klass, String key) {
        return klass.getName() + "$Accessor_" + key + "$" + accessorCount++;
    }
}