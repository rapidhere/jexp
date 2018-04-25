package ranttu.rapid.jexp.exception;

/**
 * @author rapidhere@gmail.com
 * @version $Id: UnsupportedYet.java, v0.1 2017-07-28 5:20 PM dongwei.dq Exp $
 */
public class UnsupportedYet extends JExpCompilingException {
    public UnsupportedYet(String msg) {
        super("unsupported yet: " + msg);
    }
}
