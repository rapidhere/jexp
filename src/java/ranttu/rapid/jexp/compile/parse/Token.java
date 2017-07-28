package ranttu.rapid.jexp.compile.parse;

/**
 * the lexer token
 *
 * @author rapidhere@gmail.com
 * @version $Id: Token.java, v0.1 2017-07-28 2:04 PM dongwei.dq Exp $
 */
public class Token {
    public final TokenType type;

    public final int       line, column;

    public final Object    value;

    public Token(TokenType tokenType, int line, int column, Object val) {
        this.type = tokenType;
        this.line = line;
        this.column = column;
        this.value = val;
    }

    // ~~~ getter helpers

    public String getString() {
        return (String) value;
    }

    public int getInt() {
        return (int) value;
    }

    public double getDouble() {
        return (double) value;
    }
}
