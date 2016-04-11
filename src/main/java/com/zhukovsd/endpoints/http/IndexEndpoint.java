package com.zhukovsd.endpoints.http;

import javax.servlet.RequestDispatcher;
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
@WebServlet(urlPatterns = {""})
// TODO: 11.04.2016 session cookie is not working w/o trailing slash
public class IndexEndpoint extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("WEB-INF/jsp/index.jsp");
        rd.forward(request, response);
    }
}
