/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.mvel2.MVEL;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.compile.JExpExecutable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author rapid
 * @version $Id: JExpBenchmarkBase.java, v 0.1 2017年10月01日 10:25 AM rapid Exp $
 */
abstract public class JExpBenchmarkBase extends JExpTestBase {
    private List<BenchmarkRunner>   runnerList  = new ArrayList<BenchmarkRunner>() {
                                                    {
                                                        add(new JExpRunner());
                                                        add(new AviatorRunner());
                                                        add(new MvelRunner());
                                                    }
                                                };

    private static final int        TURN_LENGTH = 10;

    private static final int        TURN_COUNT  = 10000;

    private Map<String, List<Long>> turnCostMap;

    @Test(dataProvider = "load-from-benchmark-yaml")
    public void runBenchmark(BenchmarkCaseData caseData) {
        turnCostMap = new HashMap<>();

        for (int i = 0; i < TURN_COUNT; i++) {
            runTurn(caseData);
        }

        for (String name : turnCostMap.keySet()) {
            System.out.print(name + " ===========\n");

            for (int i = 0; i < TURN_LENGTH; i++) {
                System.out.print(String.format("%4.4f ms ", (double) turnCostMap.get(name).get(i)
                                                            / (double) TURN_COUNT));
            }
            System.out.println();
        }
    }

    private void runTurn(BenchmarkCaseData caseData) {
        for (BenchmarkRunner runner : runnerList) {
            runner.compile(caseData);
        }

        Collections.shuffle(runnerList);

        for (BenchmarkRunner runner : runnerList) {
            List<Long> turnCost = turnCostMap.computeIfAbsent(runner.getName(), key -> {
                ArrayList<Long> result = new ArrayList<>(TURN_LENGTH);
                for (int i = 0; i < TURN_LENGTH; i++) {
                    result.add(0L);
                }

                return result;
            });

            for (int i = 0; i < TURN_LENGTH; i++) {
                long ms = runner.run(caseData);
                turnCost.set(i, ms + turnCost.get(i));
            }
        }
    }

    protected static class BenchmarkCaseData {
        public String jexpExpression;

        public String aviatorExpression;

        public String mvelExpression;

        public Object env;

        public String description;

        @Override
        public String toString() {
            return description;
        }
    }

    protected static abstract class BenchmarkRunner<T> {
        public T compiledStub;

        public void compile(BenchmarkCaseData caseData) {
            compiledStub = innerCompile(getExpression(caseData));
        }

        public String getName() {
            return getClass().getSimpleName();
        }

        // return micro second
        public long run(BenchmarkCaseData caseData) {
            long start = System.nanoTime();
            innerRun(caseData.env);
            long end = System.nanoTime();

            return (end - start) / 1000;
        }

        protected abstract String getExpression(BenchmarkCaseData caseData);

        protected abstract T innerCompile(String expression);

        protected abstract Object innerRun(Object env);
    }

    protected static class JExpRunner extends BenchmarkRunner<JExpExecutable> {
        @Override
        protected String getExpression(BenchmarkCaseData caseData) {
            return caseData.jexpExpression;
        }

        @Override
        protected JExpExecutable innerCompile(String expression) {
            return JExp.compile(expression);
        }

        @Override
        protected Object innerRun(Object env) {
            return compiledStub.execute(env);
        }
    }

    protected static class AviatorRunner extends BenchmarkRunner<Expression> {
        @Override
        protected String getExpression(BenchmarkCaseData caseData) {
            return caseData.aviatorExpression;
        }

        @Override
        protected Expression innerCompile(String expression) {
            return AviatorEvaluator.compile(expression);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object innerRun(Object env) {
            return compiledStub.execute((Map<String, Object>) env);
        }
    }

    protected static class MvelRunner extends BenchmarkRunner {
        @Override
        protected String getExpression(BenchmarkCaseData caseData) {
            return caseData.mvelExpression;
        }

        @Override
        protected Object innerCompile(String expression) {
            return MVEL.compileExpression(expression);
        }

        @Override
        protected Object innerRun(Object env) {
            return MVEL.executeExpression(compiledStub, env);
        }
    }

    @DataProvider(name = "load-from-benchmark-yaml")
    public Iterator<Object[]> loadFromBenchmarkYaml() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        List<BenchmarkCaseData> data = mapper.readValue(getTestResource(), mapper.getTypeFactory()
            .constructCollectionType(List.class, BenchmarkCaseData.class));

        Iterator<BenchmarkCaseData> iter = data.iterator();

        return new Iterator<Object[]>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Object[] next() {
                return new Object[] { iter.next() };
            }
        };
    }
}