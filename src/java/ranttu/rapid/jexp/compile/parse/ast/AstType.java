package ranttu.rapid.jexp.compile.parse.ast;

/**
 * the type of ast node
 * @author rapidhere@gmail.com
 * @version $Id: AstType.java, v0.1 2017-07-28 3:04 PM dongwei.dq Exp $
 */
public enum AstType {
    PRIMARY_EXP,

    BINARY_EXP,

    UNARY_EXP,

    CALL_EXP,

    // ~~~ inner usages
    // expression that load ctx arg to statck
    LOAD_CTX_EXP,
}
