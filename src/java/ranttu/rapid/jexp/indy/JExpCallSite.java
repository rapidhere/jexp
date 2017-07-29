package ranttu.rapid.jexp.indy;

import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

/**
 * @author rapidhere@gmail.com
 * @version $Id: JExpCallSite.java, v0.1 2017-07-28 9:44 PM dongwei.dq Exp $
 */
public class JExpCallSite extends MutableCallSite {
    private JIndyType indyType;

    private String    name;

    public JExpCallSite(JIndyType jIndyType, String name) {
        super(MethodType.methodType(void.class));
        this.name = name;
        indyType = jIndyType;
    }

    public void init() {

    }
}
