/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.data;

/**
 * @author rapid
 * @version $Id: SimpleContext.java, v 0.1 2017年10月13日 7:21 PM rapid Exp $
 */
@SuppressWarnings("all")
public class SimpleContext {
    private Object a;

    private Object b;

    private Object c;

    //~~~ container methods

    /**
     * Getter method for property <tt>a</tt>.
     *
     * @return property value of a.
     */
    public Object getA() {
        return a;
    }

    /**
     * Setter method for property <tt>a</tt>.
     *
     * @param a value to be assigned to property a
     */
    public void setA(Object a) {
        this.a = a;
    }

    /**
     * Getter method for property <tt>b</tt>.
     *
     * @return property value of b.
     */
    public Object getB() {
        return b;
    }

    /**
     * Setter method for property <tt>b</tt>.
     *
     * @param b value to be assigned to property b
     */
    public void setB(Object b) {
        this.b = b;
    }

    /**
     * Getter method for property <tt>c</tt>.
     *
     * @return property value of c.
     */
    public Object getC() {
        return c;
    }

    /**
     * Setter method for property <tt>c</tt>.
     *
     * @param c value to be assigned to property c
     */
    public void setC(Object c) {
        this.c = c;
    }
}