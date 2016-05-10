package com.zhukovsd.serverapp.endpoints.http;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by ZhukovSD on 07.05.2016.
 */
@WebFilter(filterName = "RequestCounterFilter", urlPatterns = {"/", "/field"})
public class RequestCounterFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        System.out.println("hi from + " + Thread.currentThread().getName() + ", url = " + ((HttpServletRequest) servletRequest).getServletPath());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
