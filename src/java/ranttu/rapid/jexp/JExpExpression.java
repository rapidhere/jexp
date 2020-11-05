package ranttu.rapid.jexp;

import lombok.experimental.var;

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

    //~~~ helper

    /**
     * a convenience for default execute method
     *
     * @see JExpExpression#execute(Object)
     */
    default <T> T exec(Object context) {
        @SuppressWarnings("unchecked") var res = (T) execute(context);
        return res;
    }
}
