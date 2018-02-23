/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

import java.util.List;

/**
 * @author dongwei.dq
 * @version $Id: FunctionExpression.java, v0.1 2017-08-03 2:38 PM dongwei.dq Exp $
 */
@Type(AstType.CALL_EXP)
@RequiredArgsConstructor
public class FunctionExpression extends AstNode {
    final public AstNode       caller;

    final public List<AstNode> parameters;

    public FunctionInfo        functionInfo;

    public String              callerIdentifier;
}
