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
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author rapid
 * @version : JExpGetPropertyCallSite.java, v 0.1 2020-10-31 10:47 PM rapid Exp $
 */
/* package-private */ class JExpGetPropertyCallSite extends JExpCallSite {
    /**
     * @see JExpGetPropertyCallSite#escapeGate(Object, Object)
     */
    private static final MethodHandle MH_ESCAPE_GATE;

    /**
     * escape gate mh binded
     */
    private final MethodHandle thisEscapeGate;

    static {
        try {
            var lookup = MethodHandles.lookup();
            MH_ESCAPE_GATE = lookup.findVirtual(JExpGetPropertyCallSite.class, "escapeGate",
                methodType(Object.class, Object.class, Object.class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public JExpGetPropertyCallSite(MethodType mt, JExpAccessorSlot slot) {
        super(mt, JExpCallSiteType.GET_PROP, slot);
        thisEscapeGate = MH_ESCAPE_GATE.bindTo(this);
    }

    @SneakyThrows
    private Object escapeGate(Object o, Object key) {
        if (o == null) {
            throw new NullPointerException();
        }

        // for map instances, use map get method
        // for list instances, use list get method
        // for array instances, use array element get
        // for other objects, access getter method
        MethodHandle mh;

        if (o instanceof Map) {
            mh = fitMap();
        } else if (o instanceof List) {
            mh = fitList();
        } else if (o.getClass().isArray()) {
            mh = fitArray(o.getClass());
        } else {
            mh = fitObject(o.getClass());
        }

        relink(mh);
        return getTarget().invoke(o, key);
    }

    private MethodHandle fitArray(Class<?> arrClass) {
        // resolve array class
        Class<?> targetArrClass;
        if (arrClass.getComponentType().isPrimitive()) {
            targetArrClass = arrClass;
        } else {
            targetArrClass = Object[].class;
        }

        // anything that is not a array, will trigger the escape gate
        return MethodHandles.guardWithTest(
            MH.IS_OF_CLASS_OR_NULL.bindTo(arrClass),
            MethodHandles.arrayElementGetter(targetArrClass)
                .asType(thisEscapeGate.type()),
            thisEscapeGate);
    }

    private MethodHandle fitList() {
        // anything that is not a list, will trigger the escape gate
        return MethodHandles.guardWithTest(
            MH.IS_INSTANCE.bindTo(List.class),
            MH.LIST_GET.asType(thisEscapeGate.type()),
            thisEscapeGate);
    }

    private MethodHandle fitMap() {
        // anything that is not a map, will trigger the escape gate
        return MethodHandles.guardWithTest(
            MH.IS_INSTANCE.bindTo(Map.class),
            MH.MAP_GET.asType(thisEscapeGate.type()),
            thisEscapeGate);
    }

    private MethodHandle fitObject(Class<?> klass) {
        var propertyHandles = MH.getAccessorMethods(klass);

        // NOTE:
        // anything that cannot fit this class, will trigger escape gate
        // null objects will not trigger escape gate, instead, produce a NPE
        var guardTest = MH.IS_OF_CLASS_OR_NULL.bindTo(klass);

        //~~~ make accessor
        // i.e.
        // mh = propertyHandles.get(key);
        // return mh.invoke(owner);

        // step 3: invoke getter handler
        var getterInvoker = MethodHandles.invoker(methodType(Object.class, Object.class));

        // step 2: swap argument position to [methodHandle, propertyOwner]
        var swapped = MethodHandles.permuteArguments(getterInvoker,
            methodType(Object.class, Object.class, MethodHandle.class), 1, 0);

        // step 1: propertyName -> getterMH
        var propGetterMH = MH.MAP_GET_OR_FIELD_NOT_FOUND
            .asType(methodType(MethodHandle.class, Map.class, Object.class))
            .bindTo(propertyHandles);

        var accessor = MethodHandles.filterArguments(swapped, 0,
            MethodHandles.identity(Object.class),
            propGetterMH);

        // compose method handle
        return MethodHandles.guardWithTest(guardTest, accessor, thisEscapeGate);
    }

    /**
     * @see JExpCallSite#init(MethodHandle)
     */
    @Override
    public void init(MethodHandle currentMh) {
        setTarget(currentMh == null ? thisEscapeGate : currentMh);
    }
}