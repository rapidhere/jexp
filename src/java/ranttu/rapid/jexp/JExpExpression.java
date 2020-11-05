package ranttu.rapid.jexp;

import java.util.Set;

/**
 * compiled jexp expression
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpExpression.java, v0.1 2017-07-27 7:46 PM dongwei.dq Exp $
 */
public interface JExpExpression {
    /**
     * execute the expression and get a result
     *
     * @param context the context the expression in
     * @return execute result
     */
    Object execute(Object context);

    /**
     * get all variable names of this expression
     *
     * @return a set of variable names
     */
    Set<String> getVariableNames();
}
