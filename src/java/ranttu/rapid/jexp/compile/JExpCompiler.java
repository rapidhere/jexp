package ranttu.rapid.jexp.compile;

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
        if(this.bindingTypes == null) {
            this.bindingTypes = new HashMap<>();
        }

        return null;
    }

    protected static class CompileOption {

    }
}
