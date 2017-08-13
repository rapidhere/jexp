/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import java.util.List;

/**
 * @author dongwei.dq
 * @version $Id: FunctionExpression.java, v0.1 2017-08-03 2:38 PM dongwei.dq Exp $
 */
@Type(AstType.CALL_EXP)
public class FunctionExpression extends AstNode {
    final public String        functionName;

    final public List<AstNode> parameters;

    public FunctionExpression(String functionName, List<AstNode> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }
}
