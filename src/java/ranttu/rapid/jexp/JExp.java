package ranttu.rapid.jexp;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.experimental.var;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.JExpCompiler;
import ranttu.rapid.jexp.compile.JExpExpression;

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
        var executable = compile(expression);

        @SuppressWarnings("unchecked")
        var result = (T) executable.execute(context);
        return result;
    }

    /**
     * compile a expression and get the compiled stub
     * @param expression  the expression to compile
     * @return            the compiled stub
     */
    public static JExpExpression compile(String expression) {
        var compiler = new JExpCompiler();
        return compiler.compile(expression);
    }

    /**
     * compile a expression and get the compiled stub
     * @param expression  the expression to compile
     * @param compileOption compiling options
     * @return            the compiled stub
     */
    public static JExpExpression compile(String expression, CompileOption compileOption) {
        var compiler = new JExpCompiler(compileOption);
        return compiler.compile(expression);
    }

    // ~~~
    // only for common test usage
    public static void main(String args[]) {
        // option
        var option = new CompileOption();
        option.inlineFunction = false;
        var expression = compile("obj.get(key)", option);

        var context = new HashMap<String, Object>();
        var map0 = new HashMap<String, String>();
        map0.put("hello", "world");
        context.put("obj", map0);
        context.put("key", "hello");
        System.out.println(expression.execute(context));

        var arr0 = new ArrayList<String>();
        arr0.add("hello");
        arr0.add("world");
        context.put("obj", arr0);
        context.put("key", 0);
        System.out.println(expression.execute(context));
    }
}