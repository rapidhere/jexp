package ranttu.rapid.jexp.common;

import lombok.experimental.UtilityClass;
import ranttu.rapid.jexp.exception.UnsupportedYet;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassReader;
import ranttu.rapid.jexp.external.org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

/**
 * common utilities
 *
 * @author rapidhere@gmail.com
 * @version $Id: $.java, v0.1 2017-07-27 7:56 PM dongwei.dq Exp $
 */
@UtilityClass
public class $ {

    /**
     * print the class from byte code
     *
     * @param className the name of the class
     * @param bytes     the bytes
     */
    public void printClass(String className, byte[] bytes) {
        if (Boolean.parseBoolean(System.getProperty("jexp.printBC"))) {
            System.out.println("========Class: " + className);
            ClassReader reader = new ClassReader(bytes);
            reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
            System.out.println();
        }
    }

    // ~~~ common helpers
    @SafeVarargs
    public <T> boolean in(T o, T... toCheck) {
        for (T c : toCheck) {
            if (o.equals(c)) {
                return true;
            }
        }

        return false;
    }

    @SafeVarargs
    public <T> boolean notIn(T o, T... toCheck) {
        return !in(o, toCheck);
    }

    public <T> T notSupport(Object s) {
        throw new UnsupportedYet(s.toString());
    }

    public <T> T shouldNotReach() {
        throw new AssertionError();
    }

    public <T> T shouldNotReach(String msg) {
        throw new AssertionError(msg);
    }

    public void should(boolean exp) {
        if (!exp) {
            throw new AssertionError();
        }
    }
}
