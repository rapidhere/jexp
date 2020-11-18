/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.exception;

/**
 * @author rapid
 * @version : JExpFunctionArgumentConvertException.java, v 0.1 2020-11-17 6:52 PM rapid Exp $
 */
public class JExpFunctionArgumentConvertException extends JExpCompilingException {
    public JExpFunctionArgumentConvertException(Class<?> from, Class<?> to) {
        super("cannot convert type from " + from.getName() + " to  " + to.getName());
    }
}