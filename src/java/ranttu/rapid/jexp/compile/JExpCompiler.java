package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.compile.parse.JExpParser;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.pass.GeneratePass;
import ranttu.rapid.jexp.compile.pass.TypeInferPass;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
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

    /** the jexp class loader */
    private static JExpClassLoader jExpClassLoader = new JExpClassLoader(
                                                       JExpCompiler.class.getClassLoader());

    /** the name count */
    private static long            nameCount       = 0;

    public JExpCompiler() {
        this.option = new CompileOption();
    }

    /**
     * compile the expression with binding types
     * @param expression        expression to compile
     * @return                  compiled expression
     * @throws JExpCompilingException   compile failed exception info
     */
    public JExpExecutable compile(String expression) throws JExpCompilingException {
        AstNode ast = JExpParser.parse(expression);

        // infer the types first
        new TypeInferPass().apply(ast);

        // generate byte codes
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String clsName = nextName();
        new GeneratePass(cw, clsName, option).apply(ast);

        // write class
        byte[] byteCodes = cw.toByteArray();

        // for debug
        // $.printClass(clsName, byteCodes);

        @SuppressWarnings("unchecked")
        Class<JExpExecutable> klass = jExpClassLoader.defineClass(clsName, byteCodes);

        try {
            return klass.newInstance();
        } catch (Exception e) {
            throw new JExpCompilingException("error when instance compiled class", e);
        }
    }

    private String nextName() {
        return "ranttu.rapid.jexp.JExpCompiledExpression$" + nameCount++;
    }
}
