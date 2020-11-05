/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ranttu.rapid.jexp.compile.closure.NameClosure;

import java.util.List;

/**
 * lambda expression node
 *
 * @author rapid
 * @version : LambdaExpression.java, v 0.1 2020-11-04 4:27 PM rapid Exp $
 */
@Type(AstType.LAMBDA_EXP)
@RequiredArgsConstructor
public class LambdaExpression extends ExpressionNode {
    /**
     * parameter, all is identifier
     */
    @NonNull
    public List<String> parameters;

    /**
     * body of the function
     */
    @NonNull
    public ExpressionNode body;

    /**
     * names under this closure
     */
    public NameClosure names;
}