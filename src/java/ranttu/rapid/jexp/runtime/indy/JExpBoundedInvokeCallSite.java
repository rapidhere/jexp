/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import lombok.SneakyThrows;
import lombok.experimental.var;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author rapid
 * @version : JExpBoundedInvokeCallSite.java, v 0.1 2020-11-02 12:43 AM rapid Exp $
 */
/* package-private */ class JExpBoundedInvokeCallSite extends JExpCallSite {
    /**
     * @see JExpBoundedInvokeCallSite#escapeGate(Object, String, Object[])
     */
    private static final MethodHandle MH_ESCAPE_GATE;

    /**
     * escape gate mh binded
     */
    private final MethodHandle thisEscapeGate;

    static {
        try {
            var lookup = MethodHandles.lookup();
            MH_ESCAPE_GATE = lookup.findVirtual(JExpBoundedInvokeCallSite.class, "escapeGate",
                methodType(Object.class, Object.class, String.class, Object[].class))
                .asType(methodType(Object.class,
                    JExpBoundedInvokeCallSite.class,
                    Object.class, Object.class,
                    Object[].class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    JExpBoundedInvokeCallSite(MethodType mt, JExpAccessorSlot slot) {
        super(mt, JExpCallSiteType.BD_INVOKE, slot);

        thisEscapeGate = MH_ESCAPE_GATE.bindTo(this);
    }

    @SneakyThrows
    private Object escapeGate(Object invokee, String methodName, Object[] args) {
        if (invokee == null) {
            throw new NullPointerException();
        }

        // NOTE:
        // anything that cannot fit this class, will trigger escape gate
        // null objects will not trigger escape gate, instead, produce a NPE
        var guardTest = MH.IS_OF_CLASS_OR_NULL.bindTo(invokee.getClass());

        // step3: invoke
        var mhInvoker = MethodHandles.invoker(methodType(Object.class, Object.class, Object[].class));

        // step2: swap argument position to [methodHandle, owner, arguments]
        // then spread: [methodHandle, owner, arg0, arg1 ...]
        var swapped = MethodHandles.permuteArguments(mhInvoker,
            methodType(Object.class, Object.class, MethodHandle.class, Object[].class),
            1, 0, 2);

        // step1: find method through map
        var methodGetMH = MH.MAP_GET
            .asType(methodType(MethodHandle.class, Map.class, Object.class))
            .bindTo(MH.getAllMethods(invokee.getClass()));

        var accessor = MethodHandles.filterArguments(swapped, 0,
            MethodHandles.identity(Object.class),
            methodGetMH,
            MethodHandles.identity(Object[].class));

        // compose method handle
        var mh = MethodHandles.guardWithTest(guardTest, accessor, thisEscapeGate);

        // relink
        relink(mh);
        return getTarget().invoke(invokee, methodName, args);
    }

    /**
     * @see JExpCallSite#init(MethodHandle)
     */
    @Override
    public void init(MethodHandle currentMh) {
        setTarget(currentMh == null ? thisEscapeGate : currentMh);
    }
}