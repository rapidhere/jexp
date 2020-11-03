/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ranttu.rapid.jexp.common.StringUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.invoke.MethodType.methodType;

/**
 * method handle utilities
 *
 * @author rapid
 * @version : MH.java, v 0.1 2020-11-01 4:27 PM rapid Exp $
 */
@UtilityClass
class MH {
    /**
     * context look up
     */
    public final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    //~~~ method handles
    /**
     * @see Class#isInstance(Object)
     */
    public final MethodHandle IS_INSTANCE;

    /**
     * @see java.util.Map#get(Object)
     */
    public final MethodHandle MAP_GET;

    /**
     * @see MH#isOfClassOrNull(Class, Object)
     */
    public final MethodHandle IS_OF_CLASS_OR_NULL;

    //~~~ caches
    private final Map<Class<?>, Map<String, MethodHandle>> accessorMethodCache = new WeakHashMap<>();

    private final Map<Class<?>, Map<String, MethodHandle>> allMethodsCache = new WeakHashMap<>();

    //~~~ accessor methods

    /**
     * get all public accessor methods
     */
    public Map<String, MethodHandle> getAccessorMethods(Class<?> klass) {
        Map<String, MethodHandle> accessors;
        if ((accessors = accessorMethodCache.get(klass)) == null) {
            synchronized (accessorMethodCache) {
                if ((accessors = accessorMethodCache.get(klass)) == null) {
                    accessors = collectAccessorMethod(klass);
                    accessorMethodCache.put(klass, accessors);
                }
            }
        }

        return accessors;
    }

    /**
     * get all public methods
     */
    @SneakyThrows
    public Map<String, MethodHandle> getAllMethods(Class<?> klass) {
        Map<String, MethodHandle> methods;
        if ((methods = allMethodsCache.get(klass)) == null) {
            synchronized (allMethodsCache) {
                if ((methods = allMethodsCache.get(klass)) == null) {
                    methods = new HashMap<>();

                    for (Method m : klass.getMethods()) {
                        // TODO: support private method access
                        m.setAccessible(true);
                        // convert to spreader invoke
                        MethodHandle mh = LOOKUP.unreflect(m)
                            .asSpreader(Object[].class, 1);
                        methods.put(m.getName(), mh);
                    }

                    allMethodsCache.put(klass, methods);
                }
            }
        }

        return methods;
    }

    @SneakyThrows
    private Map<String, MethodHandle> collectAccessorMethod(Class<?> klass) {
        Map<String, MethodHandle> res = new HashMap<>();

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

            // TODO: @dongwei.dq support private and static property
            // TODO: @dongwei.dq support field access, not only getter methods
            m.setAccessible(true);
            res.put(StringUtil.capFirst(propertyName), LOOKUP.unreflect(m));
        }

        return res;
    }

    //~~~ method handle impl
    public boolean isOfClassOrNull(Class<?> klass, Object o) {
        if (o == null) {
            return true;
        }

        return o.getClass() == klass;
    }

    //~~~ init methodHandles
    static {
        try {
            IS_INSTANCE = LOOKUP.findVirtual(Class.class, "isInstance",
                methodType(boolean.class, Object.class));
            MAP_GET = LOOKUP.findVirtual(Map.class, "get",
                methodType(Object.class, Object.class));
            IS_OF_CLASS_OR_NULL = LOOKUP.findStatic(MH.class, "isOfClassOrNull",
                methodType(boolean.class, Class.class, Object.class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}