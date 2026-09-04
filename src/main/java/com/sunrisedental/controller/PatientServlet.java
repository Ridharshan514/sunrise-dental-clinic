package com.sunrisedental.controller;

import com.sunrisedental.model.Patient;
import com.sunrisedental.service.PatientService;
import com.sunrisedental.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/patients")
public class PatientServlet extends HttpServlet {
    private final PatientService patientService = new PatientService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        List<Patient> patients = patientService.getAllPatients();
        resp.getWriter().write(JsonUtil.listToJson(patients));
    }
}
