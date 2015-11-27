package com.zhukovsd;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/field")
public class FieldServlet extends HttpServlet {
    static Field field = new Field();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FieldServletResponse response = new FieldServletResponse();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                response.addCell(i, j, field.getCell(i, j));
            }
        }

        field.getCell(1, 1).isChecked = true;


        resp.getOutputStream().write(response.toJson().getBytes());
    }
}
