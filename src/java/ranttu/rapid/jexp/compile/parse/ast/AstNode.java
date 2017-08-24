package ranttu.rapid.jexp.compile.parse.ast;

/**
 * a ast node
 * @author rapidhere@gmail.com
 * @version $Id: AstNode.java, v0.1 2017-07-28 2:59 PM dongwei.dq Exp $
 */
abstract public class AstNode {
    public final AstType                                     type;

    public boolean                                           isConstant;

    public Object                                            constantValue;

    public ranttu.rapid.jexp.external.org.objectweb.asm.Type valueType;

    public AstNode() {
        this.type = getClass().getAnnotation(Type.class).value();
    }

    public boolean is(AstType type) {
        return this.type == type;
    }

    // constant value getter
    public int getIntValue() {
        return ((Number) constantValue).intValue();
    }

    public double getDoubleValue() {
        return ((Number) constantValue).doubleValue();
    }

    public String getStringValue() {
        return String.valueOf(constantValue);
    }
}
