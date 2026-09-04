package com.sunrisedental.controller;

import com.sunrisedental.model.Bill;
import com.sunrisedental.service.BillingService;
import com.sunrisedental.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/billing")
public class BillingServlet extends HttpServlet {
    private final BillingService billingService = new BillingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String appNo = req.getParameter("appNo");
        String format = req.getParameter("format");

        if (appNo != null && !appNo.trim().isEmpty()) {
            Bill bill = billingService.getBillByAppointmentNumber(appNo);
            if (bill == null) {
                try {
                    bill = billingService.calculateAndGenerateBill(appNo);
                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
                    return;
                }
            }

            if ("receipt".equalsIgnoreCase(format)) {
                resp.setContentType("text/plain");
                resp.getWriter().write(billingService.generatePrintableReceipt(bill));
            } else {
                resp.getWriter().write(JsonUtil.toJson(bill));
            }
        } else {
            List<Bill> list = billingService.getAllBills();
            resp.getWriter().write(JsonUtil.listToJson(list));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String appNo = req.getParameter("appNo");
            Bill bill = billingService.calculateAndGenerateBill(appNo);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\":true,\"bill\":" + JsonUtil.toJson(bill) + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
        }
    }
}
