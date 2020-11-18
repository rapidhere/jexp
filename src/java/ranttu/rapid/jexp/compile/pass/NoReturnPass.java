/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.compile.pass;

import lombok.SneakyThrows;
import ranttu.rapid.jexp.common.$;
import ranttu.rapid.jexp.compile.CompilingContext;
import ranttu.rapid.jexp.compile.parse.ast.ArrayExpression;
import ranttu.rapid.jexp.compile.parse.ast.BinaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.CallExpression;
import ranttu.rapid.jexp.compile.parse.ast.DictExpression;
import ranttu.rapid.jexp.compile.parse.ast.ExpressionNode;
import ranttu.rapid.jexp.compile.parse.ast.LambdaExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqExpression;
import ranttu.rapid.jexp.compile.parse.ast.LinqFromClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqGroupByClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqJoinClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqLetClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqOrderByClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqSelectClause;
import ranttu.rapid.jexp.compile.parse.ast.LinqWhereClause;
import ranttu.rapid.jexp.compile.parse.ast.MemberExpression;
import ranttu.rapid.jexp.compile.parse.ast.PrimaryExpression;
import ranttu.rapid.jexp.compile.parse.ast.UnaryExpression;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;

/**
 * @author dongwei.dq
 * @version $Id: NoReturnPass.java, v0.1 2017-08-24 6:08 PM dongwei.dq Exp $
 */
public abstract class NoReturnPass<C> implements Pass {
    protected CompilingContext compilingContext;

    protected Deque<C> ctxStack = new ArrayDeque<>();

    @Override
    public void apply(ExpressionNode astNode, CompilingContext context) {
        this.compilingContext = context;
        prepare();
        visit(astNode);
    }

    protected void prepare() {
        // default left as blank
    }

    protected void visit(ExpressionNode astNode) {
        switch (astNode.type) {
            case PRIMARY_EXP:
                visit((PrimaryExpression) astNode);
                break;
            case BINARY_EXP:
                visit((BinaryExpression) astNode);
                break;
            case CALL_EXP:
                visit((CallExpression) astNode);
                break;
            case MEMBER_EXP:
                visit((MemberExpression) astNode);
                break;
            case ARRAY_EXP:
                visit((ArrayExpression) astNode);
                break;
            case LAMBDA_EXP:
                visit((LambdaExpression) astNode);
                break;
            case LINQ_EXP:
                visit((LinqExpression) astNode);
                break;
            case LINQ_FROM_CLAUSE:
                visit((LinqFromClause) astNode);
                break;
            case LINQ_LET_CLAUSE:
                visit((LinqLetClause) astNode);
                break;
            case LINQ_SELECT_CLAUSE:
                visit((LinqSelectClause) astNode);
                break;
            case LINQ_WHERE_CLAUSE:
                visit((LinqWhereClause) astNode);
                break;
            case LINQ_ORDERBY_CLAUSE:
                visit((LinqOrderByClause) astNode);
                break;
            case LINQ_JOIN_CLAUSE:
                visit((LinqJoinClause) astNode);
                break;
            case LINQ_GROUPBY_CLAUSE:
                visit((LinqGroupByClause) astNode);
                break;
            case DICT_EXP:
                visit((DictExpression) astNode);
                break;
            case UNARY_EXP:
                visit((UnaryExpression) astNode);
                break;
            default:
                $.notSupport(astNode.type);
        }
    }

    protected abstract void visit(PrimaryExpression exp);

    protected abstract void visit(BinaryExpression exp);

    protected abstract void visit(CallExpression exp);

    protected abstract void visit(MemberExpression exp);

    protected abstract void visit(ArrayExpression exp);

    protected abstract void visit(LambdaExpression exp);

    protected abstract void visit(LinqExpression exp);

    protected abstract void visit(LinqFromClause exp);

    protected abstract void visit(LinqLetClause exp);

    protected abstract void visit(LinqSelectClause exp);

    protected abstract void visit(LinqWhereClause exp);

    protected abstract void visit(LinqOrderByClause exp);

    protected abstract void visit(LinqJoinClause exp);

    protected abstract void visit(LinqGroupByClause exp);

    protected abstract void visit(DictExpression exp);

    protected abstract void visit(UnaryExpression exp);

    //~~~ ctx helper
    @SneakyThrows
    protected <V> V in(C ctx, Callable<V> callable) {
        try {
            ctxStack.push(ctx);
            return callable.call();
        } finally {
            ctxStack.pop();
        }
    }

    protected C ctx() {
        return ctxStack.peek();
    }
}
