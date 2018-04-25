package ranttu.rapid.jexp.compile;

/**
 * @author rapidhere@gmail.com
 * @version $Id: CompileOption.java, v0.1 2017-07-28 5:33 PM dongwei.dq Exp $
 */
public class CompileOption {
    //~~~ constants
    public static final String CURRENT_JAVA_VERSION = System
        .getProperty("java.specification.version").trim();

    public static final String JAVA_VERSION_16      = "1.6";

    public static final String JAVA_VERSION_17      = "1.7";

    //~~~ options
    /**
     * the compiled target java version
     * default to current jvm version
     */
    public String              targetJavaVersion    = JAVA_VERSION_16;

    /**
     * whether inlining functions on need
     *
     * **NOTE: this feature is still under experiment, and is not quite useful**
     */
    public boolean             inlineFunction       = false;
}
