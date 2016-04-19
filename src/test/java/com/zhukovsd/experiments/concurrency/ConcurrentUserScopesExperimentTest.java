package com.zhukovsd.experiments.concurrency;

import com.zhukovsd.Repeat;
import com.zhukovsd.RepeatRule;
import org.junit.Rule;

import static org.junit.Assert.*;

/**
 * Created by ZhukovSD on 19.04.2016.
 */
public class ConcurrentUserScopesExperimentTest {
//    static int a = 0;

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @org.junit.Test
    @Repeat( times = 100 )
    public void main() throws Exception {
        ConcurrentUserScopesExperiment.main(new String[]{});
        System.out.println("_______________");
    }
}