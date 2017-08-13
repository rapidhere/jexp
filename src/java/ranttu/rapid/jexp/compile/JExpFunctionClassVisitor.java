package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.compile.parse.ast.FunctionExpression;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

/**
 * @author rapidhere@gmail.com
 * @version $Id: JExpFunctionClassVisitor.java, v0.1 2017-08-03 4:00 PM dongwei.dq Exp $
 */
class JExpFunctionClassVisitor extends ClassVisitor implements Opcodes {
    private FunctionInfo       functionInfo;
    private FunctionExpression functionExpression;
    private JExpCompiler       compiler;
    private Label              endLabel;

    public JExpFunctionClassVisitor(FunctionInfo functionInfo,
                                    FunctionExpression functionExpression, JExpCompiler compiler,
                                    Label endLabel) {
        super(ASM5);

        this.functionInfo = functionInfo;
        this.functionExpression = functionExpression;
        this.compiler = compiler;
        this.endLabel = endLabel;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {

        if (name.equals(functionInfo.javaName)) {
            return new JExpFunctionMethodVisitor(functionInfo, functionExpression, compiler,
                endLabel);
        }

        return null;
    }
}
