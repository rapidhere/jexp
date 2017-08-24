/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function;

import java.util.Map;

/**
 * the function info of jexp function
 * @author dongwei.dq
 * @version $Id: FunctionInfo.java, v0.1 2017-08-03 3:04 PM dongwei.dq Exp $
 */
public class FunctionInfo {
    public String                name;

    public String                javaName;

    public Class                 retType;

    //~~~ for compiling usage
    public byte[]                byteCodes;

    public int                   localVarCount;

    // the number of the local variable loaded
    public Map<Integer, Integer> localVarUsedMap;
}
