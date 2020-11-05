/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * debug line numbers
 *
 * @author rapid
 * @version : DebugNo.java, v 0.1 2020-11-03 5:42 PM rapid Exp $
 */
@RequiredArgsConstructor
public enum DebugNo {
    ACC_TREE_PREPARE_START(0x0001),

    MAIN_CONTENT_START(0x0002),

    ;
    @Getter
    int flag;

    DebugNo(int flag) {
        this.flag = flag;
    }
}