package ranttu.rapid.jexp.compile.parse.ast;

import ranttu.rapid.jexp.compile.parse.Token;

/**
 * @author rapidhere@gmail.com
 * @version $Id: BinaryExpression.java, v0.1 2017-07-28 8:05 PM dongwei.dq Exp $
 */
@Type(AstType.BINARY_EXP)
public class BinaryExpression extends ExpressionNode {
    final public Token   op;

    final public ExpressionNode left, right;

    public BinaryExpression(Token op, ExpressionNode left, ExpressionNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }
}
