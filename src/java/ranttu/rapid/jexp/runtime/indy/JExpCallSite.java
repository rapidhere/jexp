/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import ranttu.rapid.jexp.common.$;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * the indy call-site for jexp
 *
 * @author rapid
 * @version $Id: JExpCallSite.java, v 0.1 2018年04月05日 4:34 PM rapid Exp $
 */
public class JExpCallSite extends MutableCallSite {
    /**
     * @see JExpCallSite#relink(Object)
     */
    private final MethodHandle MH_RELINK;

    /**
     * @see Class#isInstance(Object)
     */
    private final MethodHandle MH_IS_INSTANCE;

    /**
     * @see Map#get(Object)
     */
    private final MethodHandle MH_MAP_GET;

    /**
     * context look up
     */
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * call site type
     */
    private JExpCallSiteType ct;

    /**
     * current invokee type, init as a dummy type
     */
    private Class currentClazz = Void.class;

    /**
     * extra arg
     */
    private String arg;

    public JExpCallSite(JExpCallSiteType ct, MethodType mt, String arg) {
        super(mt);
        try {
            MH_RELINK = LOOKUP
                    .findVirtual(JExpCallSite.class, "relink", methodType(Object.class, Object.class))
                    .bindTo(this);
            MH_IS_INSTANCE = LOOKUP.findVirtual(Class.class, "isInstance",
                    methodType(boolean.class, Object.class));
            MH_MAP_GET = LOOKUP.findVirtual(Map.class, "get",
                    methodType(Object.class, Object.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.ct = ct;
        this.arg = arg;

        $.should(this.ct == JExpCallSiteType.GET_PROP);

        guard(MethodHandles.identity(Object.class));
    }

    @SuppressWarnings("unused")
    private Object relink(Object o) throws Throwable {
        if (o == null) {
            throw new NullPointerException();
        }

        currentClazz = o.getClass();
        if (o instanceof Map) {
            MethodHandle mapGetter = MethodHandles.permuteArguments(MH_MAP_GET,
                    methodType(Object.class, Object.class, Map.class), 1, 0).bindTo(arg);
            guard(mapGetter.asType(methodType(Object.class, Object.class)));
        } else {
            return null;
        }

        return getTarget().invoke(o);
    }

    private void guard(MethodHandle target) {
        setTarget(
                MethodHandles.guardWithTest(MH_IS_INSTANCE.bindTo(currentClazz), target, MH_RELINK));
    }
}