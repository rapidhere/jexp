/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.exception;

import ranttu.rapid.jexp.compile.parse.Token;

/**
 * a unexpected token occurs
 *
 * @author dongwei.dq
 * @version $Id: UnexpectedToken.java, v0.1 2017-07-28 4:23 PM dongwei.dq Exp $
 */
public class UnexpectedToken extends JExpCompilingException {
    public UnexpectedToken(Token t) {
        super("unexpected token at line " + t.line + ", column " + t.column + ": " + t.type);
    }
}
