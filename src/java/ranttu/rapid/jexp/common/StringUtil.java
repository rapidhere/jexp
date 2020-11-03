/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.common;

import lombok.experimental.UtilityClass;

/**
 * @author rapid
 * @version : StringUtil.java, v 0.1 2020-11-01 7:38 PM rapid Exp $
 */
@UtilityClass
public class StringUtil {
    /**
     * cap first letter of the string
     */
    public String capFirst(String s) {
        if (s == null || s.length() < 1) {
            return s;
        }

        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
}