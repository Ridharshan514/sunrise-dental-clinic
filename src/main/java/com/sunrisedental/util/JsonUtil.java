package com.sunrisedental.util;

import com.sunrisedental.model.*;
import java.util.List;

public class JsonUtil {

    private JsonUtil() {}

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String toJson(User user) {
        if (user == null) return "null";
        return "{\"userId\":" + user.getUserId() +
               ",\"username\":\"" + escape(user.getUsername()) + "\"" +
               ",\"fullName\":\"" + escape(user.getFullName()) + "\"" +
               ",\"role\":\"" + escape(user.getRole()) + "\"}";
    }

    public static String toJson(Patient p) {
        if (p == null) return "null";
        return "{\"patientId\":" + p.getPatientId() +
               ",\"patientName\":\"" + escape(p.getPatientName()) + "\"" +
               ",\"address\":\"" + escape(p.getAddress()) + "\"" +
               ",\"contactNumber\":\"" + escape(p.getContactNumber()) + "\"" +
               ",\"email\":\"" + escape(p.getEmail()) + "\"}";
    }

    public static String toJson(Dentist d) {
        if (d == null) return "null";
        return "{\"dentistId\":" + d.getDentistId() +
               ",\"dentistName\":\"" + escape(d.getDentistName()) + "\"" +
               ",\"specialization\":\"" + escape(d.getSpecialization()) + "\"" +
               ",\"consultationFee\":" + d.getConsultationFee() +
               ",\"contactNumber\":\"" + escape(d.getContactNumber()) + "\"}";
    }

    public static String toJson(Treatment t) {
        if (t == null) return "null";
        return "{\"treatmentId\":" + t.getTreatmentId() +
               ",\"treatmentName\":\"" + escape(t.getTreatmentName()) + "\"" +
               ",\"description\":\"" + escape(t.getDescription()) + "\"" +
               ",\"baseCost\":" + t.getBaseCost() + "}";
    }

    public static String toJson(Appointment a) {
        if (a == null) return "null";
        return "{\"appointmentId\":" + a.getAppointmentId() +
               ",\"appointmentNumber\":\"" + escape(a.getAppointmentNumber()) + "\"" +
               ",\"patientId\":" + a.getPatientId() +
               ",\"patientName\":\"" + escape(a.getPatientName()) + "\"" +
               ",\"patientAddress\":\"" + escape(a.getPatientAddress()) + "\"" +
               ",\"patientContact\":\"" + escape(a.getPatientContact()) + "\"" +
               ",\"dentistId\":" + a.getDentistId() +
               ",\"dentistName\":\"" + escape(a.getDentistName()) + "\"" +
               ",\"treatmentId\":" + a.getTreatmentId() +
               ",\"treatmentName\":\"" + escape(a.getTreatmentName()) + "\"" +
               ",\"appointmentDate\":\"" + a.getAppointmentDate() + "\"" +
               ",\"appointmentTime\":\"" + escape(a.getAppointmentTime()) + "\"" +
               ",\"status\":\"" + escape(a.getStatus()) + "\"}";
    }

    public static String toJson(Bill b) {
        if (b == null) return "null";
        return "{\"billId\":" + b.getBillId() +
               ",\"billNumber\":\"" + escape(b.getBillNumber()) + "\"" +
               ",\"appointmentNumber\":\"" + escape(b.getAppointmentNumber()) + "\"" +
               ",\"patientId\":" + b.getPatientId() +
               ",\"patientName\":\"" + escape(b.getPatientName()) + "\"" +
               ",\"dentistName\":\"" + escape(b.getDentistName()) + "\"" +
               ",\"treatmentName\":\"" + escape(b.getTreatmentName()) + "\"" +
               ",\"consultationFee\":" + b.getConsultationFee() +
               ",\"treatmentCost\":" + b.getTreatmentCost() +
               ",\"totalAmount\":" + b.getTotalAmount() +
               ",\"paymentStatus\":\"" + escape(b.getPaymentStatus()) + "\"" +
               ",\"issuedAt\":\"" + b.getIssuedAt() + "\"}";
    }

    public static <T> String listToJson(List<T> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (item instanceof User) sb.append(toJson((User) item));
            else if (item instanceof Patient) sb.append(toJson((Patient) item));
            else if (item instanceof Dentist) sb.append(toJson((Dentist) item));
            else if (item instanceof Treatment) sb.append(toJson((Treatment) item));
            else if (item instanceof Appointment) sb.append(toJson((Appointment) item));
            else if (item instanceof Bill) sb.append(toJson((Bill) item));
            else sb.append("\"").append(escape(item.toString())).append("\"");

            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
