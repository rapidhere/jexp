package ranttu.rapid.jexp.compile.parse.ast;

/**
 * a ast node
 * @author rapidhere@gmail.com
 * @version $Id: AstNode.java, v0.1 2017-07-28 2:59 PM dongwei.dq Exp $
 */
abstract public class AstNode {
    public final AstType type;

    public AstNode() {
        this.type = getClass().getAnnotation(Type.class).value();
    }

    public boolean is(AstType type) {
        return this.type == type;
    }
}
