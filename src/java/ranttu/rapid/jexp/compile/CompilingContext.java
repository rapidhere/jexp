/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import java.util.HashMap;
import java.util.Map;

/**
 * the compiling context
 * @author rapid
 * @version $Id: CompileContext.java, v 0.1 2017年09月30日 5:59 PM rapid Exp $
 */
public class CompilingContext {
    public CompileOption        option;

    public String               className;

    public String               classInternalName;

    public int                  inlinedLocalVarCount   = 0;

    public Map<String, Integer> identifierCountMap     = new HashMap<>();

    public Map<String, Integer> identifierInlineVarMap = new HashMap<>();

    public IdentifierTree       identifierTree         = new IdentifierTree();

    public JExpExecutable       compiledStub;
}