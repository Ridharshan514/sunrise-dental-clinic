package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.service.*;
import com.sunrisedental.util.JsonUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

/**
 * Standalone Embedded HTTP Server and REST API provider for Sunrise Dental Clinic.
 *
 * <p><b>Deployment Mode 1 — Standalone (Development/Demo):</b><br>
 * Run: {@code java -cp "bin;lib/*" com.sunrisedental.server.DentalAppServer}<br>
 * This starts an embedded {@code com.sun.net.httpserver.HttpServer} on port 8080,
 * serving the web dashboard and all REST API endpoints via inner handler classes.
 * Session management uses in-memory {@code ConcurrentHashMap} with HTTP-only cookies.</p>
 *
 * <p><b>Deployment Mode 2 — Apache Tomcat WAR (Production):</b><br>
 * Build: {@code mvn package} → deploy {@code target/sunrise-dental-system.war} to Tomcat.<br>
 * In this mode, {@code WEB-INF/web.xml} maps URLs to the {@code controller/*Servlet.java}
 * classes. Both modes share the same Service and DAO layers.</p>
 *
 * <p><b>DAO Persistence Strategy:</b><br>
 * The DAO layer uses graceful degradation. If MySQL is unavailable,
 * all DAOs automatically fall back to thread-safe in-memory stores,
 * ensuring the system operates during demos without a database server.
 * The {@code database/schema.sql} file provides the full relational schema
 * (including triggers and stored procedures) for production MySQL deployment.</p>
 */
public class DentalAppServer {

    private static final int PORT = 8080;
    private final AuthService authService = new AuthService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();
    private final BillingService billingService = new BillingService();
    private final ReportService reportService = new ReportService();
    private final DentistDAO dentistDAO = new DentistDAOImpl();
    private final TreatmentDAO treatmentDAO = new TreatmentDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final BillDAO billDAO = new BillDAOImpl();
    private static final Map<String, User> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        new DentalAppServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoints
        server.createContext("/api/auth", new AuthHandler());
        server.createContext("/api/appointments", new AppointmentHandler());
        server.createContext("/api/patients", new PatientHandler());
        server.createContext("/api/dentists", new DentistHandler());
        server.createContext("/api/treatments", new TreatmentHandler());
        server.createContext("/api/billing", new BillingHandler());
        server.createContext("/api/reports", new ReportHandler());
        server.createContext("/api/notifications", new NotificationHandler());
        server.createContext("/api/database", new DatabaseHandler());

        // Static Files Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default executor
        System.out.println("==================================================================");
        System.out.println(" Sunrise Dental Clinic - REST Web Services & Dashboard Running");
        System.out.println(" URL: http://localhost:" + PORT + "/");
        System.out.println(" Session Management: HTTP Cookies (SUNRISE_SESSION) & Token Auth");
        System.out.println(" Notification Engine: Automated SMS Alerts & Email Receipts");
        System.out.println("==================================================================");
        server.start();
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 1) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private static Map<String, String> parseBodyParams(HttpExchange ex) throws IOException {
        InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return parseQueryParams(sb.toString());
    }

    private static String getCookieValue(HttpExchange ex, String cookieName) {
        List<String> cookieHeaders = ex.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String header : cookieHeaders) {
                for (String cookie : header.split(";")) {
                    String[] parts = cookie.trim().split("=", 2);
                    if (parts.length == 2 && parts[0].equalsIgnoreCase(cookieName)) {
                        return parts[1];
                    }
                }
            }
        }
        return null;
    }

    private static User getAuthenticatedUser(HttpExchange ex) {
        String sess = getCookieValue(ex, "SUNRISE_SESSION");
        if (sess != null && activeSessions.containsKey(sess)) {
            return activeSessions.get(sess);
        }
        List<String> authHeaders = ex.getRequestHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = authHeaders.get(0).replace("Bearer ", "").trim();
            if (activeSessions.containsKey(token)) {
                return activeSessions.get(token);
            }
        }
        return null;
    }

    private static boolean hasRole(HttpExchange ex, String... allowedRoles) {
        User u = getAuthenticatedUser(ex);
        if (u == null) return false;
        for (String r : allowedRoles) {
            if (r.equalsIgnoreCase(u.getRole())) {
                return true;
            }
        }
        return false;
    }

    private static void sendJsonResponse(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 200, "{}");
                return;
            }
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> body = parseBodyParams(ex);
                User user = authService.login(body.get("username"), body.get("password"));
                if (user != null) {
                    String sessionId = "SUNRISE_SESS_" + UUID.randomUUID().toString().replace("-", "");
                    activeSessions.put(sessionId, user);
                    ex.getResponseHeaders().add("Set-Cookie", "SUNRISE_SESSION=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");
                    sendJsonResponse(ex, 200, "{\"success\":true,\"sessionId\":\"" + sessionId + "\",\"user\":" + JsonUtil.toJson(user) + "}");
                } else {
                    sendJsonResponse(ex, 401, "{\"success\":false,\"message\":\"Invalid username or password\"}");
                }
            } else if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
                String action = q.get("action");
                if ("check".equalsIgnoreCase(action)) {
                    String sess = getCookieValue(ex, "SUNRISE_SESSION");
                    if (sess != null && activeSessions.containsKey(sess)) {
                        User u = activeSessions.get(sess);
                        sendJsonResponse(ex, 200, "{\"authenticated\":true,\"user\":" + JsonUtil.toJson(u) + "}");
                    } else {
                        sendJsonResponse(ex, 200, "{\"authenticated\":false}");
                    }
                } else if ("logout".equalsIgnoreCase(action)) {
                    String sess = getCookieValue(ex, "SUNRISE_SESSION");
                    if (sess != null) {
                        activeSessions.remove(sess);
                    }
                    ex.getResponseHeaders().add("Set-Cookie", "SUNRISE_SESSION=; Path=/; Max-Age=0");
                    sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Logged out successfully\"}");
                } else {
                    sendJsonResponse(ex, 400, "{\"error\":\"Invalid action\"}");
                }
            } else {
                sendJsonResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class AppointmentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 200, "{}");
                return;
            }
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
                String searchQuery = q.get("q");
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    List<Appointment> results = appointmentService.searchAppointments(searchQuery);
                    sendJsonResponse(ex, 200, JsonUtil.listToJson(results));
                    return;
                }
                String appNo = q.get("appNo");
                if (appNo != null && !appNo.isEmpty()) {
                    Appointment app = appointmentService.getAppointmentDetails(appNo);
                    if (app != null) {
                        sendJsonResponse(ex, 200, JsonUtil.toJson(app));
                    } else {
                        sendJsonResponse(ex, 404, "{\"error\":\"Appointment not found\"}");
                    }
                } else {
                    sendJsonResponse(ex, 200, JsonUtil.listToJson(appointmentService.getAllAppointments()));
                }
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
                String action = q.get("action");
                User u = getAuthenticatedUser(ex);

                if ("reschedule".equalsIgnoreCase(action)) {
                    if (u != null && "DENTIST".equalsIgnoreCase(u.getRole())) {
                        sendJsonResponse(ex, 403, "{\"success\":false,\"message\":\"Access Denied: Dental surgeons cannot reschedule front-desk slots.\"}");
                        return;
                    }
                    try {
                        Map<String, String> b = parseBodyParams(ex);
                        Appointment app = appointmentService.rescheduleAppointment(
                                b.get("appNo"), b.get("appointmentDate"), b.get("appointmentTime")
                        );
                        sendJsonResponse(ex, 200, "{\"success\":true,\"appointment\":" + JsonUtil.toJson(app) + "}");
                    } catch (Exception e) {
                        sendJsonResponse(ex, 400, "{\"success\":false,\"message\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
                    }
                    return;
                } else if ("status".equalsIgnoreCase(action)) {
                    try {
                        Map<String, String> b = parseBodyParams(ex);
                        String newStatus = b.get("status");
                        if (u != null && "RECEPTIONIST".equalsIgnoreCase(u.getRole()) && ("IN_TREATMENT".equalsIgnoreCase(newStatus) || "COMPLETED".equalsIgnoreCase(newStatus))) {
                            sendJsonResponse(ex, 403, "{\"success\":false,\"message\":\"Access Denied: Only clinical dentists or administrators can mark clinical treatment states.\"}");
                            return;
                        }
                        Appointment app = appointmentService.updateStatus(b.get("appNo"), newStatus);
                        sendJsonResponse(ex, 200, "{\"success\":true,\"appointment\":" + JsonUtil.toJson(app) + "}");
                    } catch (Exception e) {
                        sendJsonResponse(ex, 400, "{\"success\":false,\"message\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
                    }
                    return;
                }

                // Standard booking
                if (u != null && "DENTIST".equalsIgnoreCase(u.getRole())) {
                    sendJsonResponse(ex, 403, "{\"success\":false,\"message\":\"Access Denied: Dental surgeons are not authorized to book appointments. Registration is restricted to Front Desk and Admin.\"}");
                    return;
                }
                try {
                    Map<String, String> b = parseBodyParams(ex);
                    Appointment app = appointmentService.registerAppointment(
                            b.get("patientName"), b.get("address"), b.get("contact"),
                            b.get("dentistName"), b.get("treatmentName"),
                            b.get("appointmentDate"), b.get("appointmentTime")
                    );
                    sendJsonResponse(ex, 201, "{\"success\":true,\"appointment\":" + JsonUtil.toJson(app) + "}");
                } catch (Exception e) {
                    sendJsonResponse(ex, 400, "{\"success\":false,\"message\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                User u = getAuthenticatedUser(ex);
                if (u != null && "DENTIST".equalsIgnoreCase(u.getRole())) {
                    sendJsonResponse(ex, 403, "{\"success\":false,\"message\":\"Access Denied: Appointment cancellation must be processed by Front Desk or Admin.\"}");
                    return;
                }
                Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
                String appNo = q.get("appNo");
                if (appNo != null && appointmentService.cancelAppointment(appNo)) {
                    sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Appointment " + appNo + " cancelled successfully.\"}");
                } else {
                    sendJsonResponse(ex, 404, "{\"success\":false,\"message\":\"Appointment not found or could not be cancelled.\"}");
                }
            } else {
                sendJsonResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class PatientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            sendJsonResponse(ex, 200, JsonUtil.listToJson(patientService.getAllPatients()));
        }
    }

    private class DentistHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            sendJsonResponse(ex, 200, JsonUtil.listToJson(dentistDAO.getAllDentists()));
        }
    }

    private class TreatmentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            sendJsonResponse(ex, 200, JsonUtil.listToJson(treatmentDAO.getAllTreatments()));
        }
    }

    private class BillingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 200, "{}");
                return;
            }
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
                String appNo = q.get("appNo");
                String format = q.get("format");
                if (appNo != null && !appNo.isEmpty()) {
                    Bill bill = billingService.getBillByAppointmentNumber(appNo);
                    if (bill == null) {
                        try {
                            bill = billingService.calculateAndGenerateBill(appNo);
                        } catch (Exception e) {
                            sendJsonResponse(ex, 404, "{\"error\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
                            return;
                        }
                    }
                    if ("receipt".equalsIgnoreCase(format)) {
                        String receipt = billingService.generatePrintableReceipt(bill);
                        byte[] bytes = receipt.getBytes(StandardCharsets.UTF_8);
                        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                        ex.sendResponseHeaders(200, bytes.length);
                        OutputStream os = ex.getResponseBody();
                        os.write(bytes);
                        os.close();
                    } else {
                        sendJsonResponse(ex, 200, JsonUtil.toJson(bill));
                    }
                } else {
                    sendJsonResponse(ex, 200, JsonUtil.listToJson(billingService.getAllBills()));
                }
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                User u = getAuthenticatedUser(ex);
                if (u != null && "DENTIST".equalsIgnoreCase(u.getRole())) {
                    sendJsonResponse(ex, 403, "{\"success\":false,\"message\":\"Access Denied: Dental surgeons are not authorized to process cashier billing. Please contact Front Desk.\"}");
                    return;
                }
                try {
                    Map<String, String> b = parseBodyParams(ex);
                    Bill bill = billingService.calculateAndGenerateBill(b.get("appNo"));
                    sendJsonResponse(ex, 200, "{\"success\":true,\"bill\":" + JsonUtil.toJson(bill) + "}");
                } catch (Exception e) {
                    sendJsonResponse(ex, 400, "{\"success\":false,\"message\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
                }
            }
        }
    }

    private class ReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
            String type = q.get("type");
            if ("schedule".equalsIgnoreCase(type)) {
                String dateStr = q.get("date");
                LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now().plusDays(1);
                sendJsonResponse(ex, 200, JsonUtil.listToJson(reportService.getDailySchedule(date)));
            } else if ("revenue".equalsIgnoreCase(type)) {
                User u = getAuthenticatedUser(ex);
                if (u != null && !"ADMIN".equalsIgnoreCase(u.getRole())) {
                    sendJsonResponse(ex, 403, "{\"error\":\"Access Denied: Executive financial revenue summaries require Administrator privileges.\"}");
                    return;
                }
                Map<String, Double> rev = reportService.getRevenueSummary();
                StringBuilder sb = new StringBuilder("{");
                int i = 0;
                for (Map.Entry<String, Double> e : rev.entrySet()) {
                    sb.append("\"").append(JsonUtil.escape(e.getKey())).append("\":").append(e.getValue());
                    if (++i < rev.size()) sb.append(",");
                }
                sb.append("}");
                sendJsonResponse(ex, 200, sb.toString());
            } else if ("workload".equalsIgnoreCase(type)) {
                Map<String, Integer> wl = reportService.getDentistWorkload();
                StringBuilder sb = new StringBuilder("{");
                int i = 0;
                for (Map.Entry<String, Integer> e : wl.entrySet()) {
                    sb.append("\"").append(JsonUtil.escape(e.getKey())).append("\":").append(e.getValue());
                    if (++i < wl.size()) sb.append(",");
                }
                sb.append("}");
                sendJsonResponse(ex, 200, sb.toString());
            } else {
                sendJsonResponse(ex, 400, "{\"error\":\"Unknown report type\"}");
            }
        }
    }

    private class NotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 200, "{}");
                return;
            }
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                User u = getAuthenticatedUser(ex);
                if (u != null && "DENTIST".equalsIgnoreCase(u.getRole())) {
                    sendJsonResponse(ex, 403, "{\"error\":\"Access Denied: Notification logs are restricted to Front Desk and Admin.\"}");
                    return;
                }
                sendJsonResponse(ex, 200, JsonUtil.listToJson(NotificationService.getRecentNotifications()));
            } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                Map<String, String> b = parseBodyParams(ex);
                String type = b.get("type");
                String recipient = b.get("recipient");
                String content = b.get("content");
                String logEntry = String.format("[%s] [%s DISPATCHED] To: %s | Message: %s",
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        type != null ? type.toUpperCase() : "ALERT",
                        recipient != null ? recipient : "Patient",
                        content != null ? content : "Notice from Sunrise Dental Clinic"
                );
                NotificationService.getRecentNotifications().add(logEntry);
                sendJsonResponse(ex, 200, "{\"success\":true,\"message\":\"Notification simulated successfully\"}");
            } else {
                sendJsonResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    private class DatabaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJsonResponse(ex, 200, "{}");
                return;
            }
            Map<String, String> q = parseQueryParams(ex.getRequestURI().getQuery());
            String table = q.get("table");
            if (table == null || table.isEmpty()) {
                String json = "{"
                    + "\"database\":\"sunrise_dental_db\","
                    + "\"server\":\"MySQL 8.0 (InnoDB)\","
                    + "\"tables\":{"
                    + "\"users\":" + JsonUtil.listToJson(userDAO.getAllUsers()) + ","
                    + "\"dentists\":" + JsonUtil.listToJson(dentistDAO.getAllDentists()) + ","
                    + "\"treatments\":" + JsonUtil.listToJson(treatmentDAO.getAllTreatments()) + ","
                    + "\"patients\":" + JsonUtil.listToJson(patientDAO.getAllPatients()) + ","
                    + "\"appointments\":" + JsonUtil.listToJson(appointmentService.getAllAppointments()) + ","
                    + "\"bills\":" + JsonUtil.listToJson(billDAO.getAllBills())
                    + "}}";
                sendJsonResponse(ex, 200, json);
            } else {
                switch (table.toLowerCase()) {
                    case "users": sendJsonResponse(ex, 200, JsonUtil.listToJson(userDAO.getAllUsers())); break;
                    case "dentists": sendJsonResponse(ex, 200, JsonUtil.listToJson(dentistDAO.getAllDentists())); break;
                    case "treatments": sendJsonResponse(ex, 200, JsonUtil.listToJson(treatmentDAO.getAllTreatments())); break;
                    case "patients": sendJsonResponse(ex, 200, JsonUtil.listToJson(patientDAO.getAllPatients())); break;
                    case "appointments": sendJsonResponse(ex, 200, JsonUtil.listToJson(appointmentService.getAllAppointments())); break;
                    case "bills": sendJsonResponse(ex, 200, JsonUtil.listToJson(billDAO.getAllBills())); break;
                    default: sendJsonResponse(ex, 404, "{\"error\":\"Table not found\"}"); break;
                }
            }
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File("src/main/webapp" + path);
            if (!file.exists() || file.isDirectory()) {
                String notFound = "<h1>404 Not Found</h1>";
                ex.sendResponseHeaders(404, notFound.length());
                OutputStream os = ex.getResponseBody();
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html";
            else if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".json")) contentType = "application/json";

            byte[] bytes = Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, bytes.length);
            OutputStream os = ex.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
