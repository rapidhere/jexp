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
    //~~~ common
    /** user specified compiling options */
    public CompileOption       option;

    /** compiling result */
    public JExpExpression      compiledStub;

    //~~~ compiling class name
    /** the standard class name */
    public String              className;

    /** internal class name */
    public String              classInternalName;

    //~~~ compiling constants
    /** constant slots count */
    public int                 constantCount = 0;

    /** constant slots map */
    public Map<Object, String> constantSlots = new HashMap<>();

    //~~~ compiling variables
    /** access tree */
    public AccessTree          accessTree;

    /** number of variables */
    public int                 variableCount = 0;

    /**
     * get the constant slot index
     */
    public String getConstantSlot(Object constantObj) {
        return constantSlots.get(constantObj);
    }

    public int nextVariableIndex() {
        return variableCount++;
    }
}