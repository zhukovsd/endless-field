package com.zhukovsd.endpoints.http;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by ZhukovSD on 07.04.2016.
 */
@WebServlet(name="online-minesweeper", urlPatterns = {"/field"})
//@WebServlet("/field")
public class FieldEndpoint extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        session.setMaxInactiveInterval(60*60*24);

        resp.getOutputStream().print("session = " + session.getId());
    }
}
