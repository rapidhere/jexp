package ranttu.rapid.jexp.compile;

/**
 * @author rapidhere@gmail.com
 * @version $Id: CompileOption.java, v0.1 2017-07-28 5:33 PM dongwei.dq Exp $
 */
public class CompileOption {
    //~~~ constants
    public static final String JAVA_VERSION_17 = "1.7";

    //~~~ options
    /**
     * the compiled target java version
     * default to current jvm version
     */
    public String targetJavaVersion = JAVA_VERSION_17;

    /**
     * turn on debug info or not
     */
    public boolean debugInfo = false;

    /**
     * property getters treated as no-side-effects
     */
    public boolean treatGetterNoSideEffect = false;

    /**
     * whether inlining functions on need
     * <p>
     * **NOTE: this feature is still under experiment, and is not quite useful**
     */
    public boolean inlineFunction = false;
}
