/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import lombok.experimental.var;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.common.AstUtil;
import ranttu.rapid.jexp.common.TypeUtil;
import ranttu.rapid.jexp.compile.PropertyTree;
import ranttu.rapid.jexp.compile.parse.TokenType;
import ranttu.rapid.jexp.compile.parse.ast.AstType;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.parse.ast.MemberExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.PropertyAccessNode;
import ranttu.rapid.jexp.exception.UnknownFunction;
import ranttu.rapid.jexp.external.org.objectweb.asm.Type;
import ranttu.rapid.jexp.runtime.function.FunctionInfo;
import ranttu.rapid.jexp.runtime.function.JExpFunctionFactory;

/**
 * do some prepare jobs
 * include:
 * type inferring
 * member expression folding
 * constant folding
 * build identifier tree
 *
 * @author dongwei.dq
 * @version $Id: TypeInferPass.java, v0.1 2017-08-24 6:06 PM dongwei.dq Exp $
 */
public class PreparePass extends NoReturnPass {
    @Override
    protected void prepare() {
        context.propertyTree = new PropertyTree(context);
    }

    @Override
    protected void visit(PrimaryExpression primary) {
        primary.isConstant = true;
        var t = primary.token;

        switch (t.type) {
            case STRING:
                primary.valueType = Type.getType(String.class);
                primary.constantValue = t.getString();
                return;
            case INTEGER:
                primary.valueType = Type.INT_TYPE;
                primary.constantValue = t.getInt();
                return;
            case FLOAT:
                primary.valueType = Type.DOUBLE_TYPE;
                primary.constantValue = t.getDouble();
                return;
            case IDENTIFIER:
                // i.e. a direct identifier load
                primary.valueType = Type.getType(Object.class);
                primary.isConstant = false;

                // build id tree
                primary.isStatic = true;
                context.propertyTree.addToRoot(primary, AstUtil.asId(primary));
                return;
            default:
                $.notSupport(t.type);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void visit(BinaryExpression exp) {
        visit(exp.left);
        visit(exp.right);

        //~~~ infer ret type
        // for String
        if (exp.op.is(TokenType.PLUS)
                && (TypeUtil.isString(exp.left.valueType) || TypeUtil.isString(exp.right.valueType))) {
            exp.valueType = Type.getType(String.class);
        }
        // number
        else {
            if ($.notIn(exp.op.type, TokenType.PLUS, TokenType.SUBTRACT, TokenType.MULTIPLY,
                    TokenType.DIVIDE, TokenType.MODULAR)) {
                $.notSupport("unknown binary op: " + exp.op);
            }

            // TODO: @dongwei.dq, refine for wrapper types
            if (!TypeUtil.isNumber(exp.left.valueType) || !TypeUtil.isNumber(exp.right.valueType)) {
                // i.e., determine at runtime
                exp.valueType = Type.getType(Object.class);
            } else {
                if (TypeUtil.isFloat(exp.left.valueType) || TypeUtil.isFloat(exp.right.valueType)) {
                    exp.valueType = Type.DOUBLE_TYPE;
                } else {
                    exp.valueType = Type.INT_TYPE;
                }
            }
        }

        //~~~ calc constant
        if (exp.left.isConstant && exp.right.isConstant) {
            exp.isConstant = true;

            if (TypeUtil.isString(exp.valueType)) {
                exp.constantValue = exp.left.getStringValue() + exp.right.getStringValue();
            } else if (TypeUtil.isFloat(exp.valueType)) {
                double leftValue = exp.left.getDoubleValue();
                double rightValue = exp.right.getDoubleValue();

                switch (exp.op.type) {
                    case PLUS:
                        exp.constantValue = leftValue + rightValue;
                        break;
                    case SUBTRACT:
                        exp.constantValue = leftValue - rightValue;
                        break;
                    case MULTIPLY:
                        exp.constantValue = leftValue * rightValue;
                        break;
                    case DIVIDE:
                        exp.constantValue = leftValue / rightValue;
                        break;
                    case MODULAR:
                        exp.constantValue = leftValue % rightValue;
                        break;
                }
            } else {
                int leftValue = exp.left.getIntValue();
                int rightValue = exp.right.getIntValue();

                switch (exp.op.type) {
                    case PLUS:
                        exp.constantValue = leftValue + rightValue;
                        break;
                    case SUBTRACT:
                        exp.constantValue = leftValue - rightValue;
                        break;
                    case MULTIPLY:
                        exp.constantValue = leftValue * rightValue;
                        break;
                    case DIVIDE:
                        exp.constantValue = leftValue / rightValue;
                        break;
                    case MODULAR:
                        exp.constantValue = leftValue % rightValue;
                        break;
                }
            }
        }
    }

    @Override
    protected void visit(CallExpression func) {
        if (AstUtil.isIdentifier(func.caller)) {
            var functionName = AstUtil.asId(func.caller);
            var infoOptional = JExpFunctionFactory.getInfo(functionName);

            if (infoOptional.isPresent()) {
                prepareUnboundedInvoke(func, infoOptional.get());
            } else {
                throw new UnknownFunction(functionName);
            }

        } else if (!asLibInvoke(func)) {
            prepareBoundedInvoke(func);
        }
    }

    private boolean asLibInvoke(CallExpression func) {
        if (func.caller.is(AstType.MEMBER_EXP)) {
            var callerMember = (MemberExpression) func.caller;

            String libName, funcName;
            // get lib name
            if (AstUtil.isIdentifier(callerMember.owner)) {
                libName = AstUtil.asId(callerMember.owner);
            } else {
                return false;
            }

            // get func name
            if (AstUtil.isExactString(callerMember.propertyName)) {
                funcName = AstUtil.asExactString(callerMember.propertyName);
            } else {
                return false;
            }

            // get function info
            var infoOptional = JExpFunctionFactory.getInfo(libName, funcName);
            if (infoOptional.isPresent()) {
                prepareUnboundedInvoke(func, infoOptional.get());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void prepareBoundedInvoke(CallExpression func) {
        $.should(func.caller.is(AstType.MEMBER_EXP));
        var callerMember = (MemberExpression) func.caller;

        $.should(AstUtil.isExactString(callerMember.propertyName));
        visit(callerMember.owner);

        // visit all parameters
        for (ExpressionNode astNode : func.parameters) {
            visit(astNode);
        }

        // TODO: this is to tricky
        func.caller = callerMember.owner;

        func.isBounded = true;
        func.accessorSlot = context.nextAccessorSlot();
        func.isConstant = false;
        func.valueType = Type.getType(Object.class);
        func.methodName = AstUtil.asExactString(callerMember.propertyName);
    }

    private void prepareUnboundedInvoke(CallExpression func, FunctionInfo info) {
        // visit all parameters
        for (ExpressionNode astNode : func.parameters) {
            visit(astNode);
        }

        // cannot infer constant value for function expressions now
        func.isBounded = false;
        func.functionInfo = info;
        func.isConstant = false;
        func.valueType = Type.getType(info.method.getReturnType());
    }

    @Override
    protected void visit(MemberExpression member) {
        visit(member.owner);
        visit(member.propertyName);

        // member expression fold
        if (member.propertyName.isConstant && member.owner instanceof PropertyAccessNode) {
            PropertyAccessNode owner = (PropertyAccessNode) member.owner;

            if (owner.isStatic) {
                member.isStatic = true;
            }
        }

        if (member.owner instanceof PropertyAccessNode) {
            PropertyAccessNode owner = (PropertyAccessNode) member.owner;
            context.propertyTree.add(owner.propertyNode, member,
                    AstUtil.asConstantString(member.propertyName));
        } else {
            context.propertyTree.addToRoot(member, AstUtil.asConstantString(member.propertyName));
        }

        // currently member expression is always not a constant
        member.isConstant = false;
        member.valueType = Type.getType(Object.class);
    }
}
