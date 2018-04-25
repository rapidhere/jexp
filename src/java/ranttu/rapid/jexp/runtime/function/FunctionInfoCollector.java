/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.function;

import ranttu.rapid.jexp.external.org.objectweb.asm.ClassReader;
import ranttu.rapid.jexp.external.org.objectweb.asm.ClassVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.MethodVisitor;
import ranttu.rapid.jexp.external.org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

/**
 * collect some function info
 *
 * @author dongwei.dq
 * @version $Id: FunctionInfoCollector.java, v0.1 2017-08-23 9:23 PM dongwei.dq Exp $
 */
class FunctionInfoCollector {
    /**
     * collect the infos of methods under a class
     */
    public static void collectInfo(byte[] byteCodes, Map<String, FunctionInfo> functionInfoMap) {
        FunctionInfoCollector collector = new FunctionInfoCollector();
        collector.functionInfoMap = functionInfoMap;
        collector.collect(byteCodes);
    }

    //~~~ impl
    private Map<String, FunctionInfo> functionInfoMap;

    private FunctionInfoCollector() {
    }

    private void collect(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        cr.accept(new InnerClassVisitor(), ClassReader.SKIP_DEBUG);
    }

    protected class InnerClassVisitor extends ClassVisitor {
        public InnerClassVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                         String[] exceptions) {
            FunctionInfo info = functionInfoMap.get(name);
            if (info != null) {
                return new InnerMethodVisitor(info);
            } else {
                return null;
            }
        }
    }

    protected class InnerMethodVisitor extends MethodVisitor implements Opcodes {
        private FunctionInfo info;

        public InnerMethodVisitor(FunctionInfo functionInfo) {
            super(ASM5);
            info = functionInfo;

            // init
            info.localVarCount = 0;
            info.localVarUsedMap = new HashMap<>();
            info.returnInsnCount = 0;
        }

        @Override
        public void visitInsn(int opcode) {
            switch (opcode) {
                case IRETURN:
                case LRETURN:
                case FRETURN:
                case DRETURN:
                case ARETURN:
                    info.returnInsnCount++;
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            info.localVarCount = Math.max(var + 1, info.localVarCount);

            // the number of load
            switch (opcode) {
                case ILOAD:
                case LLOAD:
                case FLOAD:
                case DLOAD:
                case ALOAD:
                    int cnt = info.localVarUsedMap.getOrDefault(var, 0);
                    info.localVarUsedMap.put(var, cnt + 1);
            }
        }
    }
}
