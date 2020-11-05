package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.compile.parse.JExpParser;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.pass.GeneratePass;
import ranttu.rapid.jexp.compile.pass.PreparePass;
import ranttu.rapid.jexp.exception.JExpCompilingException;

/**
 * the jexp compiler
 *
 * @author rapidhere@gmail.com
 * @version $Id: Compiler.java, v0.1 2017-07-27 7:58 PM dongwei.dq Exp $
 */
public class JExpCompiler {
    /**
     * the compile option
     */
    private final CompileOption option;

    /**
     * the name count
     */
    private static long nameCount = 0;

    public JExpCompiler() {
        this.option = new CompileOption();
    }

    public JExpCompiler(CompileOption option) {
        this.option = option;
    }

    /**
     * compile the expression with binding types
     *
     * @param expression expression to compile
     * @return compiled expression
     * @throws JExpCompilingException compile failed exception info
     */
    public JExpExpression compile(String expression) throws JExpCompilingException {
        ExpressionNode ast = JExpParser.parse(expression);
        CompilingContext compilingContext = new CompilingContext();
        compilingContext.option = option;
        compilingContext.rawExpression = expression;

        // prepares
        new PreparePass().apply(ast, compilingContext);

        // generate byte codes
        compilingContext.className = nextName();
        compilingContext.classInternalName = compilingContext.className.replace('.', '/');
        new GeneratePass().apply(ast, compilingContext);

        return compilingContext.compiledStub;
    }

    private String nextName() {
        return "ranttu.rapid.jexp.JExpCompiledExpression$" + nameCount++;
    }
}
