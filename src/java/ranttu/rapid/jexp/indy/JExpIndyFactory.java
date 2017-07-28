package ranttu.rapid.jexp.indy;

import ranttu.rapid.jexp.external.org.objectweb.asm.Handle;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * the indy factory for jExp
 * @author rapidhere@gmail.com
 * @version $Id: JExpIndyFactory.java, v0.1 2017-07-28 9:39 PM dongwei.dq Exp $
 */
final public class JExpIndyFactory {
    private JExpIndyFactory() {
    }

    public static final Handle INDY_CALLSITE = new Handle(Opcodes.H_INVOKESTATIC,
                                                 Type.getInternalName(JExpIndyFactory.class),
                                                 "callsite", MethodType.methodType(CallSite.class,
                                                     MethodHandles.Lookup.class, String.class,
                                                     MethodType.class, String.class)
                                                     .toMethodDescriptorString(), false);

    /**
     * call site factory entry for `invokedynamic` instruction
     */
    @SuppressWarnings("unused")
    public static CallSite callsite(MethodHandles.Lookup lookup, String methodName, MethodType mt,
                                    String name) {
        JIndyType type = JIndyType.valueOf(methodName);
        return new JExpCallSite(type, name);
    }
}
