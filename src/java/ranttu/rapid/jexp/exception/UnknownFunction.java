/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.exception;

/**
 * @author dongwei.dq
 * @version $Id: UnknownFunction.java, v0.1 2017-08-03 2:56 PM dongwei.dq Exp $
 */
public class UnknownFunction extends JExpCompilingException {
    public UnknownFunction(String name) {
        super("unknown function: " + name);
    }

    public UnknownFunction(String libName, String name) {
        super("unknown function: " + libName + "." + name);
    }
}
