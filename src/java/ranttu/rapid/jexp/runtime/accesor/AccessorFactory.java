/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.accesor;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.exception.JExpRuntimeException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.runtime.JExpClassLoader;
import ranttu.rapid.jexp.runtime.RuntimeUtil;

/**
 * the accessor generate factory
 *
 * @author rapid
 * @version $Id: AccessorFactory.java, v 0.1 2017年10月03日 4:40 PM rapid Exp $
 */
final public class AccessorFactory implements Opcodes {
    private AccessorFactory() {
    }

    @SuppressWarnings("unused")
    public static Accessor getAccessor(Object o) {
        return theFactory.get(o);
    }

    //~~~ impl

    private static final AccessorFactory    theFactory          = new AccessorFactory();

    private Map<String, Accessor>           accessorStore       = new HashMap<>();

    private Map<Class, Map<String, Method>> accessorMethodCache = new WeakHashMap<>();

    private Map<Class, Map<String, Method>> allMethodsCache     = new WeakHashMap<>();

    private static int                      accessorCount       = 0;

    private Accessor get(Object o) {
        if (o == null) {
            return null;
        }

        Class klass = o.getClass();
        String storedKey = klass.getName();

        return accessorStore.computeIfAbsent(storedKey, k -> generateAccessor(klass));
    }

    private Accessor generateAccessor(Class klass) {
        // use klass name, so they have same package access privilege
        var className = getAccessorName(klass);
        var classInternalName = className.replace(".", "/");

        // start define
        var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, classInternalName, null,
            getInternalName(Object.class), new String[] { getInternalName(Accessor.class) });
        cw.visitSource("<jexp-accessor>", null);

        // constructor method
        var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        generateIsSatisfied(cw, klass);
        generateGetter(cw, klass);
        generateInvoker(cw, klass);

        // generate bytecode
        cw.visitEnd();

        var bc = cw.toByteArray();
        $.printClass(className, bc);
        Class<Accessor> acKlass = JExpClassLoader.define(className, bc);

        try {
            return acKlass.newInstance();
        } catch (Throwable e) {
            throw new JExpRuntimeException("failed to generate accessor class!", e);
        }
    }

    private void generateIsSatisfied(ClassWriter cw, Class klass) {
        var mv = cw.visitMethod(ACC_PUBLIC, "isSatisfied",
            getMethodDescriptor(getType(boolean.class), getType(Object.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, getInternalName(klass));
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateGetter(ClassWriter cw, Class klass) {
        // get method
        var mv = cw.visitMethod(ACC_PUBLIC, "get", getMethodDescriptor(getType(Object.class),
            getType(Object.class), getType(String.class)), null, null);
        mv.visitCode();

        //~~~ deal with map accessor
        if (Map.class.isAssignableFrom(klass)) {
            // load map
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, getInternalName(klass));

            // load parameter
            mv.visitVarInsn(ALOAD, 2);

            // invoke map
            mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(Map.class), "get",
                getMethodDescriptor(getType(Object.class), getType(Object.class)), true);
            mv.visitInsn(ARETURN);
        }
        //~~~ common java bean
        else {
            buildHashTable(mv, groupByHashCode(getAccessorMethods(klass)), (method) -> {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, getInternalName(klass));
                if (method.getDeclaringClass().isInterface()) {
                    mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(method.getDeclaringClass()),
                        method.getName(), getMethodDescriptor(method), true);
                } else {
                    mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(method.getDeclaringClass()),
                        method.getName(), getMethodDescriptor(method), false);
                }

                wrapReturn(mv, method);
                mv.visitInsn(ARETURN);
            }, () -> {
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
            });
        }

        // end get method
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateInvoker(ClassWriter cw, Class klass) {
        var mv = cw.visitMethod(ACC_PUBLIC, "invoke", getMethodDescriptor(getType(Object.class),
            getType(Object.class), getType(String.class), getType(Object[].class)), null, null);

        var methodMap = getAllMethods(klass);

        buildHashTable(mv, groupByHashCode(methodMap), (method) -> {
            mv.visitVarInsn(ALOAD, 1);
            var parameterTypes = method.getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++) {
                // load parameter
                mv.visitVarInsn(ALOAD, 3);
                mv.visitLdcInsn(i);
                mv.visitInsn(AALOAD);

                // convert parameter
                mv.visitTypeInsn(CHECKCAST, getInternalName(parameterTypes[i]));
            }

            if (method.getDeclaringClass().isInterface()) {
                mv.visitMethodInsn(INVOKEINTERFACE, getInternalName(method.getDeclaringClass()),
                    method.getName(), getMethodDescriptor(method), true);
            } else {
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(klass), method.getName(),
                    getMethodDescriptor(method), false);
            }

            wrapReturn(mv, method);
            mv.visitInsn(ARETURN);
        }, () -> {
            mv.visitMethodInsn(INVOKESTATIC, getInternalName(RuntimeUtil.class), "noSuchMethod",
                getMethodDescriptor(getType(Object.class), getType(String.class),
                    getType(String.class)),
                false);
            mv.visitInsn(ARETURN);
        });

        // end
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void buildHashTable(MethodVisitor mv,
                                Map<Integer, Map<String, Method>> hashGroupedAccessors,
                                Consumer<Method> matched, Runnable failed) {
        // put hash table
        var hashCodes = new int[hashGroupedAccessors.size()];
        var hashLabels = new Label[hashGroupedAccessors.size()];

        // init labels
        var defaultLabel = new Label();
        var i = 0;
        for (int hash : hashGroupedAccessors.keySet()) {
            hashCodes[i] = hash;
            hashLabels[i] = new Label();
            i++;
        }

        // calc hashcode
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(Object.class), "hashCode",
            getMethodDescriptor(getType(int.class)), false);
        mv.visitLookupSwitchInsn(defaultLabel, hashCodes, hashLabels);

        for (i = 0; i < hashCodes.length; i++) {
            mv.visitLabel(hashLabels[i]);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            var methods = hashGroupedAccessors.get(hashCodes[i]);

            methods.forEach((name, method) -> {
                var failedLabel = new Label();
                mv.visitVarInsn(ALOAD, 2);
                mv.visitLdcInsn(name);
                mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(String.class), "equals",
                    getMethodDescriptor(getType(boolean.class), getType(Object.class)), false);
                mv.visitJumpInsn(IFEQ, failedLabel);

                matched.accept(method);

                mv.visitLabel(failedLabel);
                mv.visitFrame(F_SAME, 0, null, 0, null);
            });

            mv.visitJumpInsn(GOTO, defaultLabel);
        }

        // default return null
        mv.visitLabel(defaultLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        failed.run();
    }

    private void wrapReturn(MethodVisitor mv, Method m) {
        Class retType = m.getReturnType();
        if (retType == void.class) {
            mv.visitInsn(ACONST_NULL);
        } else if (retType == boolean.class) {
            wrapPrimitive(mv, boolean.class, Boolean.class);
        } else if (retType == char.class) {
            wrapPrimitive(mv, char.class, Character.class);
        } else if (retType == byte.class) {
            wrapPrimitive(mv, byte.class, Byte.class);
        } else if (retType == short.class) {
            wrapPrimitive(mv, short.class, Short.class);
        } else if (retType == int.class) {
            wrapPrimitive(mv, int.class, Integer.class);
        } else if (retType == long.class) {
            wrapPrimitive(mv, long.class, Long.class);
        } else if (retType == float.class) {
            wrapPrimitive(mv, float.class, Float.class);
        } else if (retType == double.class) {
            wrapPrimitive(mv, double.class, Double.class);
        }

        // for other non-primitive types, simply do nothing
    }

    private void wrapPrimitive(MethodVisitor mv, Class primitiveType, Class warpType) {
        mv.visitMethodInsn(INVOKESTATIC, getInternalName(warpType), "valueOf",
            getMethodDescriptor(getType(warpType), getType(primitiveType)), false);
    }

    private Map<Integer, Map<String, Method>> groupByHashCode(Map<String, Method> methods) {
        TreeMap<Integer, Map<String, Method>> result = new TreeMap<>();
        methods.forEach((name, method) -> {
            var hash = name.hashCode();
            if (!result.containsKey(hash)) {
                result.put(hash, new HashMap<>());
            }
            result.get(hash).put(name, method);
        });

        return result;
    }

    private Map<String, Method> getAccessorMethods(Class klass) {
        return accessorMethodCache.computeIfAbsent(klass, this::collectAccessorMethod);
    }

    private Map<String, Method> getAllMethods(Class klass) {
        return allMethodsCache.computeIfAbsent(klass, k -> {
            Map<String, Method> methods = new HashMap<>();

            for (Method m : k.getMethods()) {
                methods.put(m.getName(), m);
            }

            return methods;
        });
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
        return "ranttu.rapid.jexp.runtime.accesor." + klass.getName().replace('.', '_')
               + "$Accessor$" + accessorCount++;
    }
}