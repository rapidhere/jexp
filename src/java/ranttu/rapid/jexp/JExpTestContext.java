/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp;

/**
 * only for test usage
 *
 * @author rapid
 * @version $Id: JExpTestContext.java, v 0.1 2017年10月13日 4:38 PM rapid Exp $
 */
public class JExpTestContext {
    public Object getA() {
        return "hello";
    }

    public Object getB() {
        return "world";
    }

    public Object getC() {
        return 20;
    }

    public Object getO() {
        return this;
    }
}