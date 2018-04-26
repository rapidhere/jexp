/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import lombok.experimental.UtilityClass;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ast.AstType;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;

/**
 * ast related utilities
 *
 * @author rapid
 * @version $Id: AstUtil.java, v 0.1 2018年02月23日 4:25 PM rapid Exp $
 */
@UtilityClass
public class AstUtil {
    public boolean isIdentifier(ExpressionNode astNode) {
        return astNode.is(AstType.PRIMARY_EXP)
                && ((PrimaryExpression) astNode).token.is(TokenType.IDENTIFIER);
    }

    public String asId(ExpressionNode astNode) {
        $.should(isIdentifier(astNode));
        return ((PrimaryExpression) astNode).token.getString();
    }

    public boolean isExactString(ExpressionNode astNode) {
        return astNode.is(AstType.PRIMARY_EXP)
                && ((PrimaryExpression) astNode).token.is(TokenType.STRING);
    }

    public String asExactString(ExpressionNode astNode) {
        $.should(isExactString(astNode));
        return ((PrimaryExpression) astNode).token.getString();
    }

    /**
     * NOTE: can use after the constant property is resolved
     */
    public String asConstantString(ExpressionNode astNode) {
        return astNode.isConstant ? String.valueOf(astNode.constantValue) : null;
    }
}