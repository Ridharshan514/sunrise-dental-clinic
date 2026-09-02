package com.sunrisedental.controller;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.ReportService;
import com.sunrisedental.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/api/reports")
public class ReportServlet extends HttpServlet {
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String type = req.getParameter("type");
        if ("schedule".equalsIgnoreCase(type)) {
            String dateStr = req.getParameter("date");
            LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now().plusDays(1);
            List<Appointment> list = reportService.getDailySchedule(date);
            resp.getWriter().write(JsonUtil.listToJson(list));
        } else if ("revenue".equalsIgnoreCase(type)) {
            Map<String, Double> rev = reportService.getRevenueSummary();
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Double> e : rev.entrySet()) {
                sb.append("\"").append(JsonUtil.escape(e.getKey())).append("\":").append(e.getValue());
                if (++i < rev.size()) sb.append(",");
            }
            sb.append("}");
            resp.getWriter().write(sb.toString());
        } else if ("workload".equalsIgnoreCase(type)) {
            Map<String, Integer> wl = reportService.getDentistWorkload();
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Integer> e : wl.entrySet()) {
                sb.append("\"").append(JsonUtil.escape(e.getKey())).append("\":").append(e.getValue());
                if (++i < wl.size()) sb.append(",");
            }
            sb.append("}");
            resp.getWriter().write(sb.toString());
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid report type. Supported: schedule, revenue, workload\"}");
        }
    }
}
