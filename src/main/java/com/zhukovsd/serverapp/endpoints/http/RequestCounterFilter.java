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

                    System.out.println("count = " + count.get() + ", average = " + average);

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
