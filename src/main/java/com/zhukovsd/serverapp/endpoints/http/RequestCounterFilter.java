/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.serverapp.endpoints.http;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ZhukovSD on 07.05.2016.
 */
@WebFilter(filterName = "RequestCounterFilter", urlPatterns = {"/", "/field"})
public class RequestCounterFilter implements Filter {
    AtomicInteger count = new AtomicInteger();
    AtomicLong respTime = new AtomicLong();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    double average;
                    if (count.get() != 0)
                        average = (((double) respTime.get()) / ((double) count.get()));
                    else
                        average = 0;

//                    System.out.println("count = " + count.get() + ", average = " + average);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        System.out.println("hi from + " + Thread.currentThread().getName() + ", url = " + ((HttpServletRequest) servletRequest).getServletPath());
        long time = System.nanoTime();

        filterChain.doFilter(servletRequest, servletResponse);

        respTime.addAndGet((System.nanoTime() - time) / 1000000);
        count.incrementAndGet();
    }

    @Override
    public void destroy() {

    }
}
