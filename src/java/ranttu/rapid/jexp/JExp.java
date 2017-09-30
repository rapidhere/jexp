package ranttu.rapid.jexp;

import ranttu.rapid.jexp.compile.JExpCompiler;
import ranttu.rapid.jexp.compile.JExpExecutable;

import java.util.HashMap;
import java.util.Map;

/**
 * The jExp facade
 * @author rapidhere@gmail.com
 * @version $Id: JExp.java, v0.1 2017-07-27 7:41 PM dongwei.dq Exp $
 */
final public class JExp {
    private JExp() {
    }

    /**
     * eval a expression in context and get result
     * @param expression  the expression to eval
     * @param context     context the expression run in
     * @param <T>         return type
     * @return            the eval result
     */
    public static <T> T eval(String expression, Object context) {
        JExpExecutable executable = compile(expression);

        @SuppressWarnings("unchecked")
        T result = (T) executable.execute(context);
        return result;
    }

    /**
     * compile a expression and get the compiled stub
     * @param expression  the expression to compile
     * @return            the compiled stub
     */
    public static JExpExecutable compile(String expression) {
        JExpCompiler compiler = new JExpCompiler();
        return compiler.compile(expression);
    }

    // ~~~
    // only for common test usage
    public static void main(String args[]) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("a", 1);
        ctx.put("b", 3);
        Object o = eval("a + b + b * b", ctx);
        System.out.println(o);
    }
}
