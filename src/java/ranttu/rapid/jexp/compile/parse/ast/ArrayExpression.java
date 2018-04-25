/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author rapid
 * @version $Id: ArrayExpression.java, v 0.1 2018年04月26日 3:06 AM rapid Exp $
 */
@Type(AstType.ARRAY_EXP)
@RequiredArgsConstructor
public class ArrayExpression extends ExpressionNode {
    final public List<ExpressionNode> items;
}