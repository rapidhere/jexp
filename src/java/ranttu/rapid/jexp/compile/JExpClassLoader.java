package ranttu.rapid.jexp.compile;

/**
 * @author rapidhere@gmail.com
 * @version $Id: JExpClassLoader.java, v0.1 2017-07-28 5:24 PM dongwei.dq Exp $
 */
class JExpClassLoader extends ClassLoader {
    public JExpClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
