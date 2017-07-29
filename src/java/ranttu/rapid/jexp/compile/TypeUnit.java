package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

/**
 * a typing unit
 * @author rapidhere@gmail.com
 * @version $Id: TypeUnit.java, v0.1 2017-07-29 12:47 PM dongwei.dq Exp $
 */
public class TypeUnit {
    public Type    type;

    public boolean isConstant = false;

    public Object  value;
}
