/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * dict literal
 *
 * @author rapid
 * @version : DictExpression.java, v 0.1 2020-11-13 6:18 PM rapid Exp $
 */
@Type(AstType.DICT_EXP)
@RequiredArgsConstructor
public class DictExpression extends ExpressionNode {
    final public Map<String, ExpressionNode> items;
}