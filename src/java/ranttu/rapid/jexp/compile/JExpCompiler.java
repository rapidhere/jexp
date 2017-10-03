package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.compile.parse.JExpParser;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.pass.GeneratePass;
import ranttu.rapid.jexp.compile.pass.TypeInferPass;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;

/**
 * the jexp compiler
 *
 * @author rapidhere@gmail.com
 * @version $Id: Compiler.java, v0.1 2017-07-27 7:58 PM dongwei.dq Exp $
 */
public class JExpCompiler implements Opcodes {
    /** the compile option */
    private final CompileOption    option;

    /** the name count */
    private static long            nameCount       = 0;

    public JExpCompiler() {
        this.option = new CompileOption();
    }

    public JExpCompiler(CompileOption option) {
        this.option = option;
    }

    /**
     * compile the expression with binding types
     * @param expression        expression to compile
     * @return                  compiled expression
     * @throws JExpCompilingException   compile failed exception info
     */
    public JExpExecutable compile(String expression) throws JExpCompilingException {
        AstNode ast = JExpParser.parse(expression);
        CompilingContext compilingContext = new CompilingContext();
        compilingContext.option = option;

        // infer the types first
        new TypeInferPass().apply(ast, compilingContext);

        // generate byte codes
        compilingContext.className = nextName();
        new GeneratePass().apply(ast, compilingContext);

        return compilingContext.compiledStub;
    }

    private String nextName() {
        return "ranttu.rapid.jexp.JExpCompiledExpression$" + nameCount++;
    }
}
