/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package ranttu.rapid.jexp.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mvel2.MVEL;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;

import ranttu.rapid.jexp.JExp;
import ranttu.rapid.jexp.compile.JExpExpression;

/**
 * @author rapid
 * @version $Id: JExpBenchmarkBase.java, v 0.1 2017年10月01日 10:25 AM rapid Exp $
 */
abstract public class JExpBenchmarkBase {
    private static final int        TURN_LENGTH = 10;

    private static final int        TURN_COUNT  = 20;

    private Map<String, List<Long>> turnCostMap;

    @Test(dataProvider = "load-from-benchmark-yaml")
    public void runBenchmark(BenchmarkCaseData caseData) {
        turnCostMap = new HashMap<>();

        for (int i = 0; i < TURN_COUNT; i++) {
            runTurn(caseData);
        }

        for (String name : turnCostMap.keySet()) {
            System.out.print(String.format("%-17s ============\n", name));

            for (int i = 0; i < TURN_LENGTH; i++) {
                System.out.print(String.format("%10.4fms, ",
                    (double) turnCostMap.get(name).get(i) / (double) TURN_COUNT / 1000000.0));
            }
            System.out.println("\n");
        }
    }

    private void runTurn(BenchmarkCaseData caseData) {
        List<BenchmarkRunner> runnerList = new ArrayList<BenchmarkRunner>() {
            {
                add(new JExpRunner());
                add(new AviatorRunner());
                add(new MvelRunner());
            }
        };

        for (BenchmarkRunner runner : runnerList) {
            runner.compile(caseData);
            runner.prepare();
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
                long ns = runner.run(caseData);
                turnCost.set(i, ns + turnCost.get(i));
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

        public void prepare() {

        }

        // return micro second
        public long run(BenchmarkCaseData caseData) {
            long sum = 0;

            for (int i = 0; i < 30000; i++) {
                long start = System.nanoTime();
                innerRun(caseData.env);
                sum += System.nanoTime() - start;
            }

            return sum;
        }

        protected abstract String getExpression(BenchmarkCaseData caseData);

        protected abstract T innerCompile(String expression);

        protected abstract Object innerRun(Object env);
    }

    @SuppressWarnings("unused")
    protected static class JExpRunner extends BenchmarkRunner<JExpExpression> {
        @Override
        protected String getExpression(BenchmarkCaseData caseData) {
            return caseData.jexpExpression;
        }

        @Override
        protected JExpExpression innerCompile(String expression) {
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
            AviatorEvaluator.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.EVAL);
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
        List<BenchmarkCaseData> data = mapper.readValue(TestUtil.getTestResource(getClass()),
            mapper.getTypeFactory().constructCollectionType(List.class, BenchmarkCaseData.class));

        for (BenchmarkCaseData benchmarkCaseData : data) {
            benchmarkCaseData.env = TestUtil.fillObject(benchmarkCaseData.env);
        }

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