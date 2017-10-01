/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

/**
 * a jexp expression that return constant value
 * @author rapid
 * @version $Id: JExpMutableExecutable.java, v 0.1 2017年10月01日 11:20 AM rapid Exp $
 */
public class JExpMutableExpression implements JExpExecutable {
    private Object val;

    protected JExpMutableExpression() {}

    public static JExpMutableExpression of(Object val) {
        JExpMutableExpression exp = new JExpMutableExpression();
        exp.val = val;

        return exp;
    }

    @Override
    public Object execute(Object context) {
        return val;
    }
}