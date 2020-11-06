package ranttu.rapid.jexp.compile.parse;

/**
 * the token types
 *
 * @author rapidhere@gmail.com
 * @version $Id: TokenType.java, v0.1 2017-07-28 2:05 PM dongwei.dq Exp $
 */
public enum TokenType {
    /**
     * identifer
     */
    IDENTIFIER,

    /**
     * literals
     */
    INTEGER,

    FLOAT,

    STRING,

    /**
     * symbols
     */
    LEFT_PARENTHESIS,

    RIGHT_PARENTHESIS,

    LEFT_BRACKET,

    RIGHT_BRACKET,

    LEFT_BRACE,

    RIGHT_BRACE,

    POINTER,

    COMMA,

    DOT,

    FROM,

    IN,

    SELECT,

    /**
     * math
     */
    PLUS(11, true),

    SUBTRACT(11, true),

    MULTIPLY(12, true),

    DIVIDE(12, true),

    MODULAR(12, true),

    /**
     * COND
     */
    OR(10, true),

    AND(10, true),

    /**
     * fake
     */
    FAKE(Integer.MIN_VALUE, false);

    public int priority;

    public boolean binaryOp;

    TokenType() {
        priority = -100;
        binaryOp = false;
    }

    TokenType(int i, boolean binaryOp) {
        priority = i;
        this.binaryOp = binaryOp;
    }
}
