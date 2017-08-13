package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.compile.parse.ast.FunctionExpression;
import ranttu.rapid.jexp.external.org.objectweb.asm.Handle;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

/**
 * @author rapidhere@gmail.com
 * @version $Id: JExpFunctionMethodVisitor.java, v0.1 2017-08-03 3:59 PM dongwei.dq Exp $
 */
class JExpFunctionMethodVisitor extends MethodVisitor implements Opcodes {
    private FunctionInfo       functionInfo;
    private FunctionExpression functionExpression;
    private JExpCompiler       compiler;
    private MethodVisitor      cmv;
    private Label              endLabel;

    public JExpFunctionMethodVisitor(FunctionInfo functionInfo,
                                     FunctionExpression functionExpression, JExpCompiler compiler,
                                     Label endLabel) {
        super(ASM5);

        this.functionInfo = functionInfo;
        this.functionExpression = functionExpression;
        this.compiler = compiler;
        this.cmv = compiler.mv;
        this.endLabel = endLabel;
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
        // ~~~ return instructions
            case RETURN:
                // cannot return void
                $.opNotSupport(functionInfo, opcode);
                break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                cmv.visitJumpInsn(GOTO, endLabel);
                break;
            // ~~~ other instructions, pass through
            default:
                cmv.visitInsn(opcode);
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        cmv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        cmv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        cmv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        $.opNotSupport(functionInfo, INVOKEDYNAMIC);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        $.opNotSupport(functionInfo, opcode);
    }

    @Override
    public void visitLabel(Label label) {
        // TODO: @dongwei.dq
        $.opNotSupport(functionInfo, -2);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        cmv.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        $.opNotSupport(functionInfo, IINC);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        $.opNotSupport(functionInfo, TABLESWITCH);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        $.opNotSupport(functionInfo, LOOKUPSWITCH);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        $.opNotSupport(functionInfo, MULTIANEWARRAY);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // TODO: @dongwei.dq
        $.opNotSupport(functionInfo, -1);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        cmv.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
