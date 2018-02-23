/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse.ast;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * a member expression
 * @author rapid
 * @version $Id: MemberExpression.java, v 0.1 2018年02月23日 3:24 PM rapid Exp $
 */
@Type(AstType.MEMBER_EXP)
@RequiredArgsConstructor
public class MemberExpression extends AstNode {
    final public AstNode owner;

    final public AstNode propertyName;

    /** if member expression is static */
    public boolean       isStatic;

    /** link of access members, each node is either constant value, or a identifier */
    public List<AstNode> accessLink;

    public void makeStatic(AstNode owner) {
        isStatic = true;
        accessLink = new ArrayList<>();
        accessLink.add(owner);
    }

    public void makeStatic(MemberExpression owner) {
        isStatic = true;
        accessLink = new ArrayList<>(owner.accessLink);
    }
}