/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.external.org.objectweb.asm.Handle;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * invoke dynamic entry point factory
 *
 * @author rapid
 * @version $Id: JExpIndyFactory.java, v 0.1 2018年04月05日 4:09 PM rapid Exp $
 */
final public class JExpIndyFactory {
    private JExpIndyFactory() {
    }

    /**
     * bsm config
     */
    public static final Handle INDY_FACTORY_HANDLE = new Handle(
        // the invoke type, STATIC or SPECIAL or something else
        Opcodes.H_INVOKESTATIC,
        // the internal name of factory class
        Type.getInternalName(JExpIndyFactory.class),
        // the factory method name
        "bsm",
        // the factory method desc
        Type.getMethodDescriptor(
            getType(CallSite.class),
            getType(MethodHandles.Lookup.class),
            getType(String.class),
            getType(MethodType.class),
            getType(int.class)),
        // not a interface method
        false);

    /**
     * slot cache, slotNo -> slotInstance
     */
    public static final Map<Integer, JExpAccessorSlot> slots = new HashMap<>();

    /**
     * slot number pool
     */
    public static final AtomicInteger slotNoPool = new AtomicInteger(0);


    /**
     * access next slot no
     */
    public static int nextSlotNo() {
        return slotNoPool.getAndIncrement();
    }

    /**
     * get slot by slot no
     */
    private static JExpAccessorSlot getSlot(int slotNo) {
        $.should(slotNo < slotNoPool.get());

        JExpAccessorSlot slot;
        if ((slot = slots.get(slotNo)) == null) {
            synchronized (slots) {
                if ((slot = slots.get(slotNo)) == null) {
                    slot = new JExpAccessorSlot(slotNo);
                    slots.put(slotNo, slot);
                }
            }
        }

        return slot;
    }

    /**
     * the call-site entry point
     */
    public static CallSite bsm(@SuppressWarnings("unused") MethodHandles.Lookup lookup,
                               String methodName, MethodType mt, int slotNo) {
        JExpAccessorSlot slot = getSlot(slotNo);
        var site = JExpCallSite.of(JExpCallSiteType.getByMethodName(methodName), mt, slot);
        slot.addCallSite(site);

        return site;
    }
}