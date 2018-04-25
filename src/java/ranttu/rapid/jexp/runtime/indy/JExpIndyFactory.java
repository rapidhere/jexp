/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

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
     * the call-site entry point
     */
    @SuppressWarnings("unused")
    public static CallSite bsm(MethodHandles.Lookup lookup, String methodName, MethodType mt,
                               String extra) {
        return new JExpCallSite(JExpCallSiteType.getByMethodName(methodName), mt, extra);
    }
}