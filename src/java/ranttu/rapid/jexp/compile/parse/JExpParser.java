package ranttu.rapid.jexp.compile.parse;

import ranttu.rapid.jexp.compile.jflex.Lexer;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.exception.UnexpectedEOF;
import ranttu.rapid.jexp.exception.UnexpectedToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

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
    private Lexer        lexer;

    private Stack<Token> tokenStack = new Stack<>();

    private JExpParser(Reader input) {
        this.lexer = new Lexer(input);
    }

    private AstNode parse() {
        return parseBinary();
    }

    private AstNode parseBinary() {
        Stack<Token> ops = new Stack<>();
        Stack<AstNode> exps = new Stack<>();
        exps.push(parseUnary());

        while (true) {
            Token t = nextOrNull(TokenType.PLUS, TokenType.SUBTRACT, TokenType.DIVIDE,
                TokenType.MULTIPLY, TokenType.MODULAR, TokenType.RIGHT_PARENTHESIS);

            if (t == null || t.is(TokenType.RIGHT_PARENTHESIS)) {
                break;
            }

            reduceStack(ops, exps, t);
            ops.push(t);
            exps.push(parseUnary());
        }

        reduceStack(ops, exps, Token.FAKE_TOKEN);
        return exps.pop();
    }

    private void reduceStack(Stack<Token> ops, Stack<AstNode> exps, Token curOp) {
        while (exps.size() > 1) {
            if (curOp.type.priority <= ops.peek().type.priority) {
                AstNode right = exps.pop(), left = exps.pop();
                exps.push(new BinaryExpression(ops.pop(), left, right));
            } else {
                break;
            }
        }
    }

    private AstNode parseUnary() {
        Token t = peek();
        if (t.is(TokenType.LEFT_PARENTHESIS)) {
            next();
            return parseBinary();
        } else {
            return parsePrimary();
        }
    }

    private PrimaryExpression parsePrimary() {
        Token t = next(TokenType.INTEGER, TokenType.STRING, TokenType.IDENTIFIER);
        return new PrimaryExpression(t);
    }

    private Token peek() {
        try {
            Token t = _peek();
            if (t == null) {
                throw new UnexpectedEOF();
            }
            return t;

        } catch (IOException e) {
            throw new JExpCompilingException(e.getMessage(), e);
        }
    }

    private Token nextOrNull(TokenType... types) {
        try {
            Token t = _next();
            if (t == null) {
                return null;
            }

            if (types.length != 0) {
                for (TokenType type : types) {
                    if (t.is(type)) {
                        return t;
                    }
                }
                throw new UnexpectedToken(t);
            } else {
                return t;
            }
        } catch (Exception e) {
            throw new JExpCompilingException(e.getMessage(), e);
        }
    }

    private Token next(TokenType... types) {
        Token t = nextOrNull(types);
        if (t == null) {
            throw new UnexpectedEOF();
        }
        return t;
    }

    private Token _peek() throws IOException {
        if (tokenStack.isEmpty()) {
            tokenStack.push(lexer.yylex());
        }

        return tokenStack.peek();
    }

    private Token _next() throws IOException {
        if (tokenStack.isEmpty()) {
            tokenStack.push(lexer.yylex());
        }

        return tokenStack.pop();
    }
}
