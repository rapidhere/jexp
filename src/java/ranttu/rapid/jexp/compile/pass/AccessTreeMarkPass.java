/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import ranttu.rapid.jexp.common.AstUtil;
import ranttu.rapid.jexp.compile.parse.ast.ArrayExpression;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
import ranttu.rapid.jexp.compile.parse.ast.MemberExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.PropertyAccessNode;

/**
 * mark on access tree, the node that need to be stored will be found out
 *
 * @author rapid
 * @version $Id: AccessTreeMarkPass.java, v 0.1 2018年02月24日 5:51 PM rapid Exp $
 */
public class AccessTreeMarkPass extends NoReturnPass {
    @Override
    protected void visit(PrimaryExpression exp) {
        if (AstUtil.isIdentifier(exp)) {
            markAsAccessPoint(exp);
        }
    }

    @Override
    protected void visit(MemberExpression exp) {
        if (exp.isStatic) {
            markAsAccessPoint(exp);
        } else {
            visit(exp.owner);
            visit(exp.propertyName);
        }
    }

    @Override
    protected void visit(ArrayExpression exp) {
        exp.items.forEach(this::visit);
    }

    @Override
    protected void visit(BinaryExpression exp) {
        visit(exp.left);
        visit(exp.right);
    }

    @Override
    protected void visit(CallExpression exp) {
        if (exp.isBounded) {
            visit(exp.caller);
        }
        exp.parameters.forEach(this::visit);
    }

    /**
     * mark a node as access point
     */
    private void markAsAccessPoint(PropertyAccessNode propertyAccessNode) {
        propertyAccessNode.propertyNode.isAccessPoint = true;
    }
}