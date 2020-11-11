package ranttu.rapid.jexp.compile.parse.ast;

/**
 * the type of ast node
 *
 * @author rapidhere@gmail.com
 * @version $Id: AstType.java, v0.1 2017-07-28 3:04 PM dongwei.dq Exp $
 */
public enum AstType {
    PRIMARY_EXP,

    BINARY_EXP,

    UNARY_EXP,

    CALL_EXP,

    MEMBER_EXP,

    ARRAY_EXP,

    LAMBDA_EXP,

    COMMA_EXP,

    LINQ_EXP,

    LINQ_FROM_CLAUSE,

    LINQ_LET_CLAUSE,

    LINQ_WHERE_CLAUSE,

    LINQ_ORDERBY_CLAUSE,

    LINQ_JOIN_CLAUSE,

    LINQ_SELECT_CLAUSE,

    LINQ_GROUPBY_CLAUSE,
}
