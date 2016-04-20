package com.zhukovsd.experiments.performance;

import java.util.function.Function;

/**
 * Created by ZhukovSD on 20.04.2016.
 */
public class MethodVsLambdaExperiment {
    public static void main(String[] args) {
        int count = 100000000; // 100M

        AbstractUsualOperator usualOperator = new UsualOperator();
        long time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            usualOperator.calculate(i);
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("usual = " + time);

        //

        LambdaOperator lambdaOperator = new LambdaOperator();
        Function<Integer, Integer> function = integer -> integer + 1;

        time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            lambdaOperator.calculate(i, function);
        }
        time = (System.nanoTime() - time) / 1000000;

        System.out.println("lambda = " + time);
    }
}

abstract class AbstractUsualOperator {
    abstract protected Integer apply(Integer argument);

    Integer calculate(Integer argument) {
        return apply(argument);
    }
}

class UsualOperator extends AbstractUsualOperator {
    @Override
    protected Integer apply(Integer argument) {
        return argument + 1;
    }
}

class LambdaOperator {
    Integer calculate(Integer argument, Function<Integer, Integer> function) {
        return function.apply(argument);
    }
}