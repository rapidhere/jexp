/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;

/**
 * @author rapid
 * @version : LinqGroupByClause.java, v 0.1 2020-11-09 4:20 PM rapid Exp $
 */
@Type(AstType.LINQ_GROUPBY_CLAUSE)
@RequiredArgsConstructor
public class LinqGroupByClause extends LinqFinalQueryClause {
    final public ExpressionNode selectExp;

    final public ExpressionNode keyExp;

    public LambdaExpression selectLambda;

    public LambdaExpression keyLambda;
}