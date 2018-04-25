package ranttu.rapid.jexp.exception;

/**
 * unexpected end of file
 *
 * @author rapidhere@gmail.com
 * @version $Id: UnexpectedEOF.java, v0.1 2017-07-28 4:20 PM dongwei.dq Exp $
 */
public class UnexpectedEOF extends JExpCompilingException {
    public UnexpectedEOF() {
        super("unexpected end of file");
    }
}
