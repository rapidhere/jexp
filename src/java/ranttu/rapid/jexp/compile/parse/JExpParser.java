package ranttu.rapid.jexp.compile.parse;

import ranttu.rapid.jexp.compile.jflex.Lexer;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.exception.UnexpectedEOF;
import ranttu.rapid.jexp.exception.UnexpectedToken;

import java.io.Reader;
import java.io.StringReader;

/**
 * the parser
 * @author rapidhere@gmail.com
 * @version $Id: JExpParser.java, v0.1 2017-07-28 2:58 PM dongwei.dq Exp $
 */
public class JExpParser {
    /**
     * parse the input and return a ast
     * @param input  the input text
     * @return       ast
     */
    public static AstNode parse(String input) throws JExpCompilingException {
        return new JExpParser(new StringReader(input)).parse();
    }

    // ~~ impl
    // the lexer
    private Lexer lexer;

    private JExpParser(Reader input) {
        this.lexer = new Lexer(input);
    }

    private AstNode parse() {
        return parsePrimary();
    }

    private PrimaryExpression parsePrimary() {
        Token t = next(TokenType.INTEGER, TokenType.STRING, TokenType.IDENTIFIER);
        return new PrimaryExpression(t);
    }

    private Token next(TokenType... types) {
        try {
            Token t = lexer.yylex();
            if (t == null) {
                throw new UnexpectedEOF();
            }

            for (TokenType type : types) {
                if (t.type == type) {
                    return t;
                }
            }
            throw new UnexpectedToken(t);
        } catch (Exception e) {
            throw new JExpCompilingException(e.getMessage(), e);
        }
    }
}
