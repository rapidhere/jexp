/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

/**
 * @author rapidhere@gmai.com
 * @version $Id: CaseData.java, v0.1 2017-07-28 7:00 PM dongwei.dq Exp $
 */
public class CaseData {
    public String  exp;

    public String  desc;

    public Object  res;

    public Object  ctx;

    public boolean skip = false;

    @Override
    public String toString() {
        return desc;
    }
}
