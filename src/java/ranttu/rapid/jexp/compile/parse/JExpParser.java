package ranttu.rapid.jexp.compile.parse;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.AstUtil;
import ranttu.rapid.jexp.compile.jflex.Lexer;
import ranttu.rapid.jexp.compile.parse.ast.ArrayExpression;
import ranttu.rapid.jexp.compile.parse.ast.AstType;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
import ranttu.rapid.jexp.compile.parse.ast.CommaExpression;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.parse.ast.LambdaExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqFromClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqJoinClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqLetClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqOrderByClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqSelectClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqWhereClause;
import ranttu.rapid.jexp.compile.parse.ast.MemberExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.exception.UnexpectedEOF;
import ranttu.rapid.jexp.exception.UnexpectedToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * the parser
 * <p>
 * EXP |=
 * BINARY_EXP
 * UNARY_EXP
 * MEMBER_EXP
 * FUNCTION_EXP
 * PRIMARY_EXP
 * LAMBDA_EXP
 * COMMA_EXP
 * <p>
 * COMMA_EXP |=
 * EXP
 * EXP,COMMA_EXP
 * <p>
 * BINARY_EXP |=
 * UNARY_EXP + UNARY_EXP
 * UNARY_EXP - UNARY_EXP
 * UNARY_EXP * UNARY_EXP
 * UNARY_EXP / UNARY_EXP
 * UNARY_EXP % UNARY_EXP
 * <p>
 * UNARY_EXP |=
 * MEMBER_EXP
 * FUNCTION_EXP
 * PRIMARY_EXP
 * LAMBDA_EXP
 * (EXP)
 * <p>
 * MEMBER_EXP |=
 * UNARY_EXP.IDENTIFIER
 * UNARY_EXP[EXP]
 * <p>
 * FUNCTION_EXP |=
 * UNARY_EXP(ARGUMENT_LIST)
 * <p>
 * LAMBDA_EXP |=
 * (IDENTIFIER_LIST)=>EXP
 * (IDENTIFIER_LIST)=>{EXP}
 * <p>
 * IDENTIFIER_LIST |=
 * NIL
 * IDENTIFIER_LIST, IDENTIFIER
 * <p>
 * ARGUMENT_LIST |=
 * NIL
 * ARGUMENT_LIST, EXP
 * <p>
 * PRIMARY_EXP |=
 * INTEGER
 * FLOAT
 * STRING
 * IDENTIFIER
 * ARRAY_EXP
 * <p>
 * ARRAY_EXP |=
 * NIL
 * ARRAY_EXP, EXP
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpParser.java, v0.1 2017-07-28 2:58 PM dongwei.dq Exp $
 */
public class JExpParser {
    /**
     * parse the input and return a ast
     *
     * @param input the input text
     * @return ast
     */
    public static ExpressionNode parse(String input) throws JExpCompilingException {
        return new JExpParser(new StringReader(input)).parse();
    }

    // ~~ impl
    // the lexer
    private Lexer lexer;

    private Stack<Token> tokenStack = new Stack<>();

    private JExpParser(Reader input) {
        this.lexer = new Lexer(input);
    }

    private ExpressionNode parse() {
        ExpressionNode ret = parseExp();

        Token t = nextOrNull();
        if (t != null) {
            throw new UnexpectedToken(t);
        }

        return ret;
    }

    private ExpressionNode parseExp() {
        var res = new ArrayList<ExpressionNode>();

        do {
            res.add(parseBinary());
            var t = peekOrNull();

            if (t == null || !t.is(TokenType.COMMA)) {
                break;
            }
            next(TokenType.COMMA);
        } while (true);

        if (res.size() == 1) {
            return res.get(0);
        } else {
            return new CommaExpression(res);
        }
    }

    private ExpressionNode parseBinary() {
        Stack<Token> ops = new Stack<>();
        Stack<ExpressionNode> exps = new Stack<>();
        exps.push(parseUnary());

        while (true) {
            Token t = peekOrNull();
            // not a math oper, break
            if (t == null || !t.type.binaryOp) {
                break;
            }
            // eat the token
            next();

            reduceStack(ops, exps, t);
            ops.push(t);
            exps.push(parseUnary());
        }

        reduceStack(ops, exps, Token.FAKE_TOKEN);
        return exps.pop();
    }

    private void reduceStack(Stack<Token> ops, Stack<ExpressionNode> exps, Token curOp) {
        while (exps.size() > 1) {
            if (curOp.type.priority <= ops.peek().type.priority) {
                ExpressionNode right = exps.pop(), left = exps.pop();
                exps.push(new BinaryExpression(ops.pop(), left, right));
            } else {
                break;
            }
        }
    }

    /**
     * unary-class expressions are made with those basic elements:
     * <p>
     * PRIMARY_EXP
     * (EXP)
     * `.`
     * `[...]`
     * `(...)`
     */
    private ExpressionNode parseUnary() {
        var exp = parseUnaryClassElement();
        while (true) {
            var t = peekOrNull();

            // EOF, end of parse
            if (t == null) {
                break;
            }

            // static member expression
            if (t.is(TokenType.DOT)) {
                next();
                var token = peek();
                var identifier = parsePrimary();
                if (!AstUtil.isIdentifier(identifier)) {
                    throw new UnexpectedToken(token);
                }

                // cast id to str
                Token idToken = ((PrimaryExpression) identifier).token;
                Token strToken = new Token(TokenType.STRING, idToken.line, idToken.column,
                    idToken.value);

                exp = new MemberExpression(exp, new PrimaryExpression(strToken));
            }
            // dynamic member expression
            else if (t.is(TokenType.LEFT_BRACKET)) {
                next();
                exp = new MemberExpression(exp, parseExp());
                next(TokenType.RIGHT_BRACKET);
            }
            // function expression
            else if (t.is(TokenType.LEFT_PARENTHESIS)) {
                next();
                exp = new CallExpression(exp, parseItems(TokenType.RIGHT_PARENTHESIS));
            }
            // lambda expression
            else if (t.is(TokenType.POINTER)) {
                next();
                List<String> pars;
                if (exp.is(AstType.PRIMARY_EXP)) {
                    pars = new ArrayList<>();
                    if (!AstUtil.isIdentifier(exp)) {
                        throw new UnexpectedToken(t);
                    }
                    pars.add(AstUtil.asId(exp));
                } else if (exp.is(AstType.COMMA_EXP)) {
                    //noinspection ConstantConditions
                    pars = ((CommaExpression) exp).expressions.stream()
                        .map(AstUtil::asId).collect(Collectors.toList());
                } else {
                    throw new UnexpectedToken(t);
                }

                if (peek().is(TokenType.LEFT_BRACE)) {
                    next();
                    var items = parseItems(TokenType.RIGHT_BRACE);
                    if (items.size() != 1) {
                        $.notSupport("comma expression or empty expression");
                    }

                    exp = new LambdaExpression(pars, items.get(0));
                } else {
                    exp = new LambdaExpression(pars, parseExp());
                }
            }
            // or, parse is end
            else {
                break;
            }
        }

        return exp;
    }

    /**
     * the element of unary-class expressions, can be:
     * <p>
     * (EXP)
     * PRIMARY_EXP
     */
    private ExpressionNode parseUnaryClassElement() {
        var t = peek();

        if (t.is(TokenType.LEFT_PARENTHESIS)) {
            // eat up `(`
            next();
            List<ExpressionNode> exps = parseItems(TokenType.RIGHT_PARENTHESIS);

            if (exps.size() == 1) {
                return exps.get(0);
            } else {
                return new CommaExpression(exps);
            }
        } else if (t.is(TokenType.FROM)) {
            return parseLinq();
        } else {
            return parsePrimary();
        }
    }

    private List<ExpressionNode> parseItems(TokenType endTokenType) {
        var t = peek();
        // meet end, break
        if (t.is(endTokenType)) {
            next();
            return new ArrayList<>();
        }

        // parse as a comma expression
        ExpressionNode exp = parseExp();
        next(endTokenType);
        if (exp.is(AstType.COMMA_EXP)) {
            return ((CommaExpression) exp).expressions;
        } else {
            List<ExpressionNode> pars = new ArrayList<>();
            pars.add(exp);
            return pars;
        }
    }

    /**
     * see also: https://linq.andronova.de/tag/bnf/
     * <p>
     * query-expression ::= from-clause query-body
     * <p>
     * query-body ::=
     * <p>
     * query-body-clause* final-query-clause query-continuation?
     * <p>
     * query-body-clause ::=
     * (from-clause
     * | join-clause
     * | let-clause
     * | where-clause
     * | orderby-clause)
     * <p>
     * from-clause ::= from itemName in srcExpr
     * <p>
     * join-clause ::= join itemName in srcExpr on keyExpr equals keyExpr
     * (into itemName)?
     * <p>
     * let-clause ::= let itemName = selExpr
     * <p>
     * where-clause ::= where predExpr
     * <p>
     * orderby-clause ::= orderby (keyExpr (ascending | descending)?)*
     * <p>
     * final-query-clause ::=
     * (select-clause | groupby-clause)
     * <p>
     * select-clause ::= select selExpr
     * <p>
     * groupby-clause ::= group selExpr by keyExpr
     * <p>
     * query-continuation ::= into itemName query-body
     */
    private LinqExpression parseLinq() {
        var linqExp = new LinqExpression();

        linqExp.queryBodyClauses.add(parseLinqFrom());
        do {
            switch (peek().type) {
                case FROM:
                    linqExp.queryBodyClauses.add(parseLinqFrom());
                    break;
                case LET:
                    linqExp.queryBodyClauses.add(parseLinqLet());
                    break;
                case WHERE:
                    linqExp.queryBodyClauses.add(parseLinqWhere());
                    break;
                case ORDERBY:
                    linqExp.queryBodyClauses.add(parseLinqOrderBy());
                    break;
                case JOIN:
                    linqExp.queryBodyClauses.add(parseLinqJoin());
                    break;
                case SELECT:
                    linqExp.finalQueryClause = parseLinqSelect();
                    break;
                default:
                    throw new UnexpectedToken(peek());
            }
        } while (linqExp.finalQueryClause == null);

        return linqExp;
    }

    private LinqJoinClause parseLinqJoin() {
        next(TokenType.JOIN);

        var id = next(TokenType.IDENTIFIER);
        next(TokenType.IN);

        var sourceExpr = parseExp();
        next(TokenType.ON);

        var leftKeyExpr = parseExp();
        next(TokenType.EQUALS);

        var rightKeyExpr = parseExp();

        // group join
        String groupId = null;
        if (peek().is(TokenType.INTO)) {
            next();
            groupId = next(TokenType.IDENTIFIER).getString();
        }

        return new LinqJoinClause(id.getString(), groupId,
            sourceExpr, leftKeyExpr, rightKeyExpr);
    }

    private LinqOrderByClause parseLinqOrderBy() {
        next(TokenType.ORDERBY);

        // parse items
        var items = new ArrayList<LinqOrderByClause.OrderByItem>();
        do {
            var orderByExp = parseBinary();
            var t = peek();
            var descending = false;

            if (t.is(TokenType.ASCENDING)) {
                next();
                t = peek();
            } else if (t.is(TokenType.DESCENDING)) {
                next();
                descending = true;
                t = peek();
            }

            items.add(LinqOrderByClause.item(orderByExp, descending));

            if (t.is(TokenType.COMMA)) {
                next();
            } else {
                break;
            }
        } while (true);

        return new LinqOrderByClause(items);
    }

    private LinqFromClause parseLinqFrom() {
        next(TokenType.FROM);
        var id = next(TokenType.IDENTIFIER);
        next(TokenType.IN);

        return new LinqFromClause(id.getString(), parseExp());
    }

    private LinqSelectClause parseLinqSelect() {
        next(TokenType.SELECT);

        return new LinqSelectClause(parseExp());
    }

    private LinqWhereClause parseLinqWhere() {
        next(TokenType.WHERE);

        return new LinqWhereClause(parseExp());
    }

    private LinqLetClause parseLinqLet() {
        next(TokenType.LET);

        var id = next(TokenType.IDENTIFIER);
        next(TokenType.EQ);

        return new LinqLetClause(id.getString(), parseExp());
    }

    private ExpressionNode parsePrimary() {
        var t = peek();

        // i.e., an array expression
        if (t.is(TokenType.LEFT_BRACKET)) {
            next();
            return new ArrayExpression(parseItems(TokenType.RIGHT_BRACKET));
        }
        // common primary expression
        else {
            t = next(TokenType.INTEGER, TokenType.STRING, TokenType.IDENTIFIER, TokenType.FLOAT);
            return new PrimaryExpression(t);
        }
    }

    private Token peek() {
        try {
            var t = _peek();
            if (t == null) {
                throw new UnexpectedEOF();
            }
            return t;

        } catch (IOException e) {
            throw new JExpCompilingException(e.getMessage(), e);
        }
    }

    private Token peekOrNull() {
        try {
            return _peek();

        } catch (IOException e) {
            throw new JExpCompilingException(e.getMessage(), e);
        }
    }

    private Token nextOrNull(TokenType... types) {
        try {
            var t = _next();
            if (t == null) {
                return null;
            }

            if (types.length != 0) {
                for (var type : types) {
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
        var t = nextOrNull(types);
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
