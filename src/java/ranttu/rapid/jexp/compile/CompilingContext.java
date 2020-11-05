/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.JExpExpression;
import ranttu.rapid.jexp.compile.closure.NameClosure;

/**
 * the compiling context
 *
 * @author rapid
 * @version $Id: CompileContext.java, v 0.1 2017年09月30日 5:59 PM rapid Exp $
 */
public class CompilingContext {
    //~~~ common
    /**
     * user specified compiling options
     */
    public CompileOption option;

    /**
     * the raw expression string
     */
    public String rawExpression;

    /**
     * compiling result
     */
    public JExpExpression compiledStub;


    //~~~ compiling variables
    /**
     * the root name closure
     */
    public NameClosure names;
}