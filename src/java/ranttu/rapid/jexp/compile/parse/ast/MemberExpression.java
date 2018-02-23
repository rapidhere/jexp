/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.AllArgsConstructor;

/**
 * a member expression
 * @author rapid
 * @version $Id: MemberExpression.java, v 0.1 2018年02月23日 3:24 PM rapid Exp $
 */
@Type(AstType.MEMBER_EXP)
@AllArgsConstructor
public class MemberExpression extends AstNode {
    public AstNode owner;

    public AstNode propertyName;
}