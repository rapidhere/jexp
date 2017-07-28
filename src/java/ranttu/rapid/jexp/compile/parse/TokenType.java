package ranttu.rapid.jexp.compile.parse;

/**
 * the token types
 * @author rapidhere@gmail.com
 * @version $Id: TokenType.java, v0.1 2017-07-28 2:05 PM dongwei.dq Exp $
 */
public enum TokenType {
    /** identifer */
    IDENTIFIER,

    /** literals */
    INTEGER,

    FLOAT,

    STRING,

    /** symbols */
    LEFT_PARENTHESIS,

    RIGHT_PARENTHESIS,

    LEFT_BRACKET,

    RIGHT_BRACKET,

    PLUS,
}
