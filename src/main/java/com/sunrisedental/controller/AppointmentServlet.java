package com.sunrisedental.controller;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/appointments")
public class AppointmentServlet extends HttpServlet {
    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String appNo = req.getParameter("appNo");
        if (appNo != null && !appNo.trim().isEmpty()) {
            Appointment app = appointmentService.getAppointmentDetails(appNo);
            if (app != null) {
                resp.getWriter().write(JsonUtil.toJson(app));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Appointment not found\"}");
            }
        } else {
            List<Appointment> list = appointmentService.getAllAppointments();
            resp.getWriter().write(JsonUtil.listToJson(list));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String patientName = req.getParameter("patientName");
            String address = req.getParameter("address");
            String contact = req.getParameter("contact");
            String dentistName = req.getParameter("dentistName");
            String treatmentName = req.getParameter("treatmentName");
            String date = req.getParameter("appointmentDate");
            String time = req.getParameter("appointmentTime");

            Appointment app = appointmentService.registerAppointment(patientName, address, contact, dentistName, treatmentName, date, time);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"success\":true,\"appointment\":" + JsonUtil.toJson(app) + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
        }
    }
}
