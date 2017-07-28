package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.compile.parse.JExpParser;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;

import java.util.HashMap;
import java.util.Map;

/**
 * the jexp compiler
 *
 * @author rapidhere@gmail.com
 * @version $Id: Compiler.java, v0.1 2017-07-27 7:58 PM dongwei.dq Exp $
 */
public class JExpCompiler {
    /**
     * the id-type binding offered by user
     */
    private Map<String, Class> bindingTypes;

    /**
     * the compile option
     */
    private CompileOption      option;

    public JExpCompiler() {
        this.option = new CompileOption();
    }

    public JExpExecutable compile(String expression, Map<String, Class> bindingTypes) {
        this.bindingTypes = bindingTypes;
        if (this.bindingTypes == null) {
            this.bindingTypes = new HashMap<>();
        }

        // get ast node
        AstNode ast = JExpParser.parse(expression);

        return null;
    }

    // compiler option object
    protected static class CompileOption {

    }
}
