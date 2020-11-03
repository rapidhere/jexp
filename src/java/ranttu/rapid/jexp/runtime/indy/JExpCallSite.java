/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import lombok.Getter;
import ranttu.rapid.jexp.common.$;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

/**
 * the indy call-site for jexp
 *
 * @author rapid
 * @version $Id: JExpCallSite.java, v 0.1 2018年04月05日 4:34 PM rapid Exp $
 */
abstract /* package-private */ class JExpCallSite extends MutableCallSite {
    /**
     * call site type
     */
    @Getter
    private final JExpCallSiteType ct;

    /**
     * related slot
     */
    @Getter
    private final JExpAccessorSlot slot;

    /**
     * constructor
     */
    public static JExpCallSite of(JExpCallSiteType ct, MethodType mt, JExpAccessorSlot slot) {
        JExpCallSite res;
        switch (ct) {
            case GET_PROP:
                res = new JExpGetPropertyCallSite(mt, slot);
                break;
            case BD_INVOKE:
                res = new JExpBoundedInvokeCallSite(mt, slot);
                break;
            default:
                return $.notSupport(ct);
        }

        return res;
    }

    /* package-private */ JExpCallSite(MethodType mt, JExpCallSiteType ct, JExpAccessorSlot slot) {
        super(mt);
        this.ct = ct;
        this.slot = slot;
    }

    /**
     * relink method handle via slot
     */
    final protected void relink(MethodHandle mh) {
        slot.relink(mh);
    }

    abstract public void init(MethodHandle currentMh);
}