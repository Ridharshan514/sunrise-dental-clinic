package com.sunrisedental.controller;

import com.sunrisedental.dao.DentistDAOImpl;
import com.sunrisedental.dao.TreatmentDAOImpl;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/dentists")
public class DentistServlet extends HttpServlet {
    private final DentistDAOImpl dentistDAO = new DentistDAOImpl();
    private final TreatmentDAOImpl treatmentDAO = new TreatmentDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String type = req.getParameter("type");
        if ("treatments".equalsIgnoreCase(type)) {
            List<Treatment> list = treatmentDAO.getAllTreatments();
            resp.getWriter().write(JsonUtil.listToJson(list));
        } else {
            List<Dentist> list = dentistDAO.getAllDentists();
            resp.getWriter().write(JsonUtil.listToJson(list));
        }
    }
}
