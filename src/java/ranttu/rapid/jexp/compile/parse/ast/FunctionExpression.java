/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import ranttu.rapid.jexp.runtime.function.FunctionInfo;

import java.util.List;

/**
 * @author dongwei.dq
 * @version $Id: FunctionExpression.java, v0.1 2017-08-03 2:38 PM dongwei.dq Exp $
 */
@Type(AstType.CALL_EXP)
public class FunctionExpression extends AstNode {
    public String        functionName;

    final public List<AstNode> parameters;

    public FunctionInfo        functionInfo;

    public String              callerIdentifier;

    public FunctionExpression(String functionName, List<AstNode> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }
}
