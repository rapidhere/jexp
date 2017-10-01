package ranttu.rapid.jexp.compile;

/**
 * @author rapidhere@gmail.com
 * @version $Id: JExpClassLoader.java, v0.1 2017-07-28 5:24 PM dongwei.dq Exp $
 */
public class JExpClassLoader extends ClassLoader {
    @SuppressWarnings("unchecked")
    public static Class<JExpExecutable> define(String name, byte[] b) {
        return cl.defineClass(name, b);
    }

    private static JExpClassLoader cl = new JExpClassLoader(JExpCompiler.class.getClassLoader());

    // ~~~ impl
    private JExpClassLoader(ClassLoader parent) {
        super(parent);
    }

    private Class defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
