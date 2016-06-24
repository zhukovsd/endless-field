package com.zhukovsd.experiments.concurrency.concurrentcellupdating;

import java.util.concurrent.*;

/**
 * Created by ZhukovSD on 04.04.2016.
 */
public class RejectedExecutionHandlerTest {
    public static void main(String[] args) {
        // Test shows how queue saturation and caller runs policy affects tasks run order. Rejected task will run
        // in caller thread and thus will execute earlier than task, wailing for polling from queue.

        ExecutorService exec = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());


        for (int i = 0; i < 10; i++) {
            exec.submit(
                    new Runnable() {
                        private int anonVar;

                        @Override
                        public void run()  {
                            System.out.println("entered into #" + anonVar + ", thread name = " + Thread.currentThread().getName());
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("quit from #" + anonVar);
                        }

                        private Runnable init(int var){
                            anonVar = var;
                            return this;
                        }
                    }.init(i)
            );
        }
    }
}
