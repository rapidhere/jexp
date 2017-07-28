package ranttu.rapid.jexp.exception;

/**
 * the compiling exception
 * @author rapidhere@gmail.com
 * @version $Id: JExpCompilingException.java, v0.1 2017-07-28 4:18 PM dongwei.dq Exp $
 */
public class JExpCompilingException extends JExpBaseException {
    public JExpCompilingException(String message) {
        super(message);
    }

    public JExpCompilingException(String message, Throwable e) {
        super(message, e);
    }
}
