package ranttu.rapid.jexp.compile.parse.ast;

import ranttu.rapid.jexp.compile.parse.Token;

/**
 * the primary expression like identifier, string, integers
 *
 * @author rapidhere@gmail.com
 * @version $Id: PrimaryExpression.java, v0.1 2017-07-28 4:03 PM dongwei.dq Exp $
 */
@Type(AstType.PRIMARY_EXP)
public class PrimaryExpression extends PropertyAccessNode {
    /**
     * the token hold by this primary expression
     */
    final public Token token;

    public PrimaryExpression(Token token) {
        this.token = token;
    }
}
