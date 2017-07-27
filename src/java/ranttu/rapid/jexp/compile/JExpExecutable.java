package ranttu.rapid.jexp.compile;

/**
 * compiled jexp that can execute
 *
 * @author rapidhere@gmail.com
 * @version $Id: JExpExecutable.java, v0.1 2017-07-27 7:46 PM dongwei.dq Exp $
 */
public interface JExpExecutable {
    /**
     * execute the expression and get a result
     *
     * @param context the context the expression in
     * @return execute result
     */
    Object execute(Object context);
}
