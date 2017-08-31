/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile;

import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.TypeUtil;
import ranttu.rapid.jexp.compile.parse.ast.AstNode;
import ranttu.rapid.jexp.compile.pass.GeneratePass;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassReader;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Handle;
import ranttu.rapid.jexp.external.org.objectweb.asm.Label;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the visitor working on the bytecode
 * @author dongwei.dq
 * @version $Id: JExpByteCodeTransformer.java, v0.1 2017-08-22 9:56 PM dongwei.dq Exp $
 */
public class JExpByteCodeTransformer implements Opcodes {
    public static void transform(FunctionInfo functionInfo, GeneratePass pass, MethodVisitor cmv,
                                 List<AstNode> parameters) {
        JExpByteCodeTransformer transformer = new JExpByteCodeTransformer();
        transformer.cmv = cmv;
        transformer.functionInfo = functionInfo;
        transformer.parameters = parameters;
        transformer.pass = pass;

        transformer.transform();
    }

    //~~~ impl
    private FunctionInfo  functionInfo;

    private List<AstNode> parameters;

    private ClassReader   cr;

    private MethodVisitor cmv;

    private GeneratePass  pass;

    private Label         endLabel = new Label();

    private void transform() {
        cr = new ClassReader(functionInfo.byteCodes);

        accept(new TransformPass());

        // put the end label
        cmv.visitLabel(endLabel);
        cmv.visitFrame(F_SAME1, 0, null, 1,
            new Object[] { TypeUtil.getFrameDesc(functionInfo.retType) });
    }

    private void accept(MethodVisitor mv) {
        cr.accept(new InnerClassVisitor(mv), ClassReader.SKIP_DEBUG);
    }

    /**
     * only usage is to wrap up
     */
    private class InnerClassVisitor extends ClassVisitor {
        private MethodVisitor mv;

        public InnerClassVisitor(MethodVisitor visitor) {
            super(ASM5);
            mv = visitor;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                         String[] exceptions) {

            if (name.equals(functionInfo.javaName)) {
                return mv;
            }

            return null;
        }
    }

    /**
     * the pass that transform the codes
     */
    private class TransformPass extends MethodVisitor {
        private Map<Integer, Integer> functionVarInlineMap = new HashMap<>();
        private int                   numberStored         = 0;

        public TransformPass() {
            super(ASM5);
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
            switch (opcode) {
                case ILOAD:
                case LLOAD:
                case FLOAD:
                case DLOAD:
                case ALOAD:
                    // get the variable load count
                    int inlineVar = functionVarInlineMap.getOrDefault(var, -1);
                    // i.e., the var is already loaded and stored
                    if (inlineVar >= 0) {
                        cmv.visitVarInsn(ALOAD, inlineVar);
                    } else {
                        // this is a variable in parameter, and this is the first time we meet it
                        if (var < parameters.size()) {
                            // first, put the variable on the stack
                            pass.visitOnStack(parameters.get(var));
                            // if we'll use it in the future, we store it
                            if (functionInfo.localVarUsedMap.getOrDefault(var, 0) > 1) {
                                // calculate the slot
                                int currentStoreVar = functionInfo.localVarCount + numberStored;
                                numberStored++;
                                functionVarInlineMap.put(var, currentStoreVar);

                                // gen bytecodes
                                cmv.visitInsn(DUP);
                                cmv.visitVarInsn(ASTORE, currentStoreVar);
                            }
                        }
                        // just deal it normal
                        else {
                            cmv.visitVarInsn(opcode, var);
                        }
                    }
                    break;

                default:
                    // other is store
                    cmv.visitVarInsn(opcode, var);
            }
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
            cmv.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            cmv.visitLabel(label);
        }

        @Override
        public void visitFrame(int frame, int nLocal, Object[] locals, int nStack, Object[] stacks) {
            cmv.visitFrame(frame, nLocal, locals, nStack, stacks);
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
}
