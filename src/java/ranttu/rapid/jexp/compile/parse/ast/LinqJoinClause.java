/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;

/**
 * @author rapid
 * @version : LINQ_JOIN_CLAUSE.java, v 0.1 2020-11-09 4:20 PM rapid Exp $
 */
@Type(AstType.LINQ_JOIN_CLAUSE)
@RequiredArgsConstructor
public class LinqJoinClause extends LinqQueryBodyClause {
    final public String innerItemName;

    final public String groupJoinItemName;

    final public ExpressionNode sourceExp;

    final public ExpressionNode outerKeyExp;

    final public ExpressionNode innerKeyExp;

    public LambdaExpression outerKeyLambda;

    public LambdaExpression innerKeyLambda;

    public int innerItemLinqParameterIndex;

    public int groupJoinItemLinqParameterIndex;

    public boolean isGroupJoin() {
        return groupJoinItemName != null;
    }
}