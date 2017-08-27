/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.TypeUtil;
import ranttu.rapid.jexp.compile.CompileOption;
import ranttu.rapid.jexp.compile.JExpByteCodeTransformer;
import ranttu.rapid.jexp.compile.JExpExecutable;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.FunctionExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.exception.JExpCompilingException;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassWriter;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getInternalName;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getMethodDescriptor;
import static ranttu.rapid.jexp.external.org.objectweb.asm.Type.getType;

/**
 * the pass that generate byte codes
 * @author dongwei.dq
 * @version $Id: GeneratePass.java, v0.1 2017-08-24 10:16 PM dongwei.dq Exp $
 */
public class GeneratePass extends NoReturnPass implements Opcodes {
    private ClassWriter   cw;

    private MethodVisitor mv;

    private String        className;

    private CompileOption option;

    public GeneratePass(ClassWriter cw, String className, CompileOption option) {
        this.cw = cw;
        this.className = className;
        this.option = option;
    }

    @Override
    public void apply(AstNode root) {
        // prepare class
        visitClass(className.replace('.', '/'));

        // visit the method
        super.apply(root);

        // return
        if (root.valueType == Type.INT_TYPE) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false);
        } else if (root.valueType == Type.DOUBLE_TYPE) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf",
                "(D)Ljava/lang/Double;", false);
        } else if (root.valueType == Type.BOOLEAN_TYPE) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf",
                "(Z)Ljava/lang/Boolean;", false);
        }
        mv.visitInsn(ARETURN);

        // end
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
    }

    /**
     * visit and put the ast on the stack
     */
    public void visitOnStack(AstNode astNode) {
        visit(astNode);
    }

    private void visitClass(String name) {
        if (option.targetJavaVersion.equals(CompileOption.JAVA_VERSION_16)) {
            cw.visit(V1_6, ACC_SYNTHETIC + ACC_SUPER + ACC_PUBLIC, name, null,
                getInternalName(Object.class),
                new String[] { getInternalName(JExpExecutable.class) });
            cw.visitSource("<jexp-gen>", null);

            // construct method
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            // `execute` method
            mv = cw.visitMethod(ACC_SYNTHETIC + ACC_PUBLIC, "execute",
                getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
            mv.visitParameter("this", 0);
            mv.visitParameter("context", 0);
            mv.visitCode();
        } else {
            throw new JExpCompilingException("unknown java version" + option.targetJavaVersion);
        }
    }

    @Override
    protected void visit(AstNode astNode) {
        // constant short cut
        if (astNode.isConstant) {
            mv.visitLdcInsn(astNode.constantValue);
            return;
        }
        // pass through
        super.visit(astNode);
    }

    @Override
    protected void visit(PrimaryExpression exp) {
        // primary expression is always constant
        $.shouldNotReach();
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected void visit(BinaryExpression exp) {
        // for float type
        if (TypeUtil.isFloat(exp.valueType)) {
            visit(exp.left);
            if (TypeUtil.isInt(exp.left.valueType)) {
                mv.visitInsn(I2D);
            }

            visit(exp.right);
            if (TypeUtil.isInt(exp.right.valueType)) {
                mv.visitInsn(I2D);
            }

            switch (exp.op.type) {
                case PLUS:
                    mv.visitInsn(DADD);
                    break;
                case SUBTRACT:
                    mv.visitInsn(DSUB);
                    break;
                case MULTIPLY:
                    mv.visitInsn(DMUL);
                    break;
                case DIVIDE:
                    mv.visitInsn(DDIV);
                    break;
                case MODULAR:
                    mv.visitInsn(DREM);
                    break;
            }
        }
        // for int type
        else if (TypeUtil.isInt(exp.valueType)) {
            visit(exp.left);
            visit(exp.right);

            switch (exp.op.type) {
                case PLUS:
                    mv.visitInsn(IADD);
                    break;
                case SUBTRACT:
                    mv.visitInsn(ISUB);
                    break;
                case MULTIPLY:
                    mv.visitInsn(IMUL);
                    break;
                case DIVIDE:
                    mv.visitInsn(IDIV);
                    break;
                case MODULAR:
                    mv.visitInsn(IREM);
                    break;
            }
        }
    }

    @Override
    protected void visit(FunctionExpression func) {
        FunctionInfo info = func.functionInfo;

        if (info.inline) {
            JExpByteCodeTransformer.transform(info, this, mv, func);
        } else {
            $.notSupport("current only supports inline functions!");
        }
    }
}
