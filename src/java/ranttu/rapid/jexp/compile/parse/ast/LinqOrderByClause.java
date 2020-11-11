/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author rapid
 * @version : LinqOrderByClause.java, v 0.1 2020-11-09 4:20 PM rapid Exp $
 */
@Type(AstType.LINQ_ORDERBY_CLAUSE)
@RequiredArgsConstructor
public class LinqOrderByClause extends LinqQueryBodyClause {
    public final List<OrderByItem> items;

    public static OrderByItem item(ExpressionNode exp, boolean descending) {
        return new OrderByItem(descending, exp);
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderByItem {
        public final boolean descending;

        public final ExpressionNode exp;

        public LambdaExpression keySelectLambda;
    }
}