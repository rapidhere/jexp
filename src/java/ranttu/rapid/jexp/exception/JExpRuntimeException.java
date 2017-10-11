/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.exception;

/**
 * @author rapid
 * @version $Id: JExpRuntimeException.java, v 0.1 2017年10月03日 10:20 PM rapid Exp $
 */
public class JExpRuntimeException extends JExpBaseException {
    public JExpRuntimeException(String message) {
        super(message);
    }

    public JExpRuntimeException(String message, Throwable e) {
        super(message, e);
    }
}