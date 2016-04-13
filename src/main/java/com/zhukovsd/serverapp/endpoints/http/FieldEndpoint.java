package com.zhukovsd.serverapp.endpoints.http;

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
@WebServlet(urlPatterns = {"/field"})
public class FieldEndpoint extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // don't create new session on this request. if now session found, report error to the client
        HttpSession session = request.getSession(false);

        if (session != null) {
            response.getOutputStream().print("session = " + session.getId());
        } else {
            // TODO: 12.04.2016 report no session error
        }
    }
}
