package ranttu.rapid.jexp.compile.parse.ast;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.compile.parse.ValueType;

/**
 * a ast node
 *
 * @author rapidhere@gmail.com
 * @version $Id: ExpressionNode.java, v0.1 2017-07-28 2:59 PM dongwei.dq Exp $
 */
abstract public class ExpressionNode {
    //~~~ common
    /**
     * the ast type of this expression node
     */
    public final AstType type;

    /**
     * the result value type of this expression
     */
    public ValueType valueType;

    //~~~ constant value related
    /**
     * is this expression node a constant value
     */
    public boolean isConstant;

    /**
     * only valid if isConstant is true
     */
    public Object constantValue;

    public ExpressionNode() {
        this.type = getClass().getAnnotation(Type.class).value();
    }

    public boolean is(AstType type) {
        return this.type == type;
    }

    //~~~ constant value getter
    public int intConstant() {
        $.should(isConstant);
        return ((Number) constantValue).intValue();
    }

    public double floatConstant() {
        $.should(isConstant);
        return ((Number) constantValue).doubleValue();
    }

    public String stringConstant() {
        $.should(isConstant);
        return String.valueOf(constantValue);
    }
}
