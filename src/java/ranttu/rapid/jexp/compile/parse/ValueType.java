/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.parse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

/**
 * the value type of a expression node
 *
 * @author rapid
 * @version : ValueType.java, v 0.1 2020-11-17 5:56 PM rapid Exp $
 */
@RequiredArgsConstructor
public enum ValueType {
    BOOL(Type.BOOLEAN_TYPE),

    BOOL_WRAPPED(Type.getType(Boolean.class)),

    INT_WRAPPED(Type.getType(Integer.class)),

    DOUBLE_WRAPPED(Type.getType(Double.class)),

    STRING(Type.getType(String.class)),

    STRING_BUILDER(Type.getType(StringBuilder.class)),

    ARRAY(Type.getType(List.class)),

    DICT(Type.getType(Map.class)),

    GENERIC(Type.getType(Object.class)),

    ;
    @Getter
    private final Type type;
}