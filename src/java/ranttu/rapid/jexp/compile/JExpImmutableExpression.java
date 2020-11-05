/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.JExpExpression;

import java.util.Collections;
import java.util.Set;

/**
 * a jexp expression that return constant value
 *
 * @author rapid
 * @version $Id: JExpImmutableExpression.java, v 0.1 2017年10月01日 11:20 AM rapid Exp $
 */
public class JExpImmutableExpression implements JExpExpression {
    private Object val;

    protected JExpImmutableExpression() {
    }

    public static JExpImmutableExpression of(Object val) {
        JExpImmutableExpression exp = new JExpImmutableExpression();
        exp.val = val;

        return exp;
    }

    @Override
    public Object execute(Object context) {
        return val;
    }

    @Override
    public Set<String> getVariableNames() {
        return Collections.emptySet();
    }
}