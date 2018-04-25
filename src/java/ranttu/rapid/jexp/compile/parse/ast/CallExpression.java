/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

import java.util.List;

/**
 * @author rapid
 * @version $Id: UnboundedCallExpression.java, v 0.1 2018年04月24日 4:45 PM rapid Exp $
 */
@Type(AstType.CALL_EXP)
@RequiredArgsConstructor
public class CallExpression extends ExpressionNode {
    @NonNull
    public ExpressionNode caller;

    @NonNull
    public List<ExpressionNode> parameters;

    public boolean isBounded = false;

    //~~~ unbounded invoke
    public FunctionInfo functionInfo;

    //~~~ bounded invoke
    public String methodName;

    /**
     * for bounded invoke, there will be a accessor slot
     */
    public String accessorSlot;
}