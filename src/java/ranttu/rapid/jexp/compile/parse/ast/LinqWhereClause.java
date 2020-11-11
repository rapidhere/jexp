/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;

/**
 * @author rapid
 * @version : LinqWhereClause.java, v 0.1 2020-11-09 4:20 PM rapid Exp $
 */
@Type(AstType.LINQ_WHERE_CLAUSE)
@RequiredArgsConstructor
public class LinqWhereClause extends LinqQueryBodyClause {
    final public ExpressionNode whereExp;

    public LambdaExpression lambdaExp;
}