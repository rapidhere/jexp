/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.data;

/**
 * @author rapid
 * @version $Id: RecursiveContext.java, v 0.1 2017年10月13日 7:51 PM rapid Exp $
 */
@SuppressWarnings("all")
public class ImmutableRecursiveContext {
    public Object getA() {
        return 12312312;
    }

    public Object getB() {
        return -9798;
    }

    public Object getC() {
        return 77777;
    }

    public Object getO() {
        return this;
    }

    public Object getSomeVal() {
        return "233";
    }
}