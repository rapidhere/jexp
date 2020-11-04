/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * // TODO: support comma expression execute
 * comma expression
 *
 * @author rapid
 * @version : CommaExpression.java, v 0.1 2020-11-04 4:33 PM rapid Exp $
 */
@Type(AstType.COMMA_EXP)
@RequiredArgsConstructor
public class CommaExpression extends ExpressionNode {
    /**
     * list of expressions
     */
    @NonNull
    public List<ExpressionNode> expressions;
}