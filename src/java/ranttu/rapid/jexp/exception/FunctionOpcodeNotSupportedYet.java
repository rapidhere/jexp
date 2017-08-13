/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.exception;

/**
 * @author dongwei.dq
 * @version $Id: FunctionOpcodeNotSupportedYet.java, v0.1 2017-08-03 3:50 PM dongwei.dq Exp $
 */
public class FunctionOpcodeNotSupportedYet extends JExpCompilingException {
    public FunctionOpcodeNotSupportedYet(String name, int opcode) {
        super("opcode " + opcode + " in function: " + name + " is not supported");
    }
}
