/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.indy;

import ranttu.rapid.jexp.common.$;

/**
 * @author rapid
 * @version $Id: JExpCallSiteType.java, v 0.1 2018年04月05日 4:35 PM rapid Exp $
 */
public enum JExpCallSiteType {
    /**
     * get a property
     */
    GET_PROP("GP"),

    /**
     * unbounded invoke
     */
    UB_INVOKE("UI"),

    /**
     * bounded invoke
     */
    BD_INVOKE("BI"),;
    /**
     * the method name string
     */
    String methodName;

    JExpCallSiteType(String methodName) {
        this.methodName = methodName;
    }

    public static JExpCallSiteType getByMethodName(String mn) {
        for (JExpCallSiteType type : values()) {
            if (mn.equals(type.methodName)) {
                return type;
            }
        }

        return $.shouldNotReach("unknown method type:" + mn);
    }
}