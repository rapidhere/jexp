/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;
import ranttu.rapid.jexp.compile.parse.Token;

/**
 * @author rapid
 * @version : UnaryExpression.java, v 0.1 2020-11-18 2:12 PM rapid Exp $
 */
@Type(AstType.UNARY_EXP)
@RequiredArgsConstructor
public class UnaryExpression extends ExpressionNode {
    public final Token op;

    public final ExpressionNode exp;
}