/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * a accessor slot
 *
 * @author rapid
 * @version : JExpAccessorSlot.java, v 0.1 2020-11-01 11:09 PM rapid Exp $
 */
@RequiredArgsConstructor
public class JExpAccessorSlot {
    /**
     * slot no of this slot
     */
    @Getter
    private final int slotNo;

    /**
     * related cs
     */
    @Getter
    private final List<JExpCallSite> relateCallSites = new ArrayList<>(16);

    /**
     * current method handle
     */
    private MethodHandle currentMh = null;

    /**
     * add a call site to this slot
     */
    public synchronized void addCallSite(JExpCallSite cs) {
        relateCallSites.add(cs);
        cs.init(currentMh);
    }

    /**
     * relink method handle to all call-sites
     */
    public synchronized void relink(MethodHandle mh) {
        relateCallSites.forEach(cs -> cs.setTarget(mh));
    }
}