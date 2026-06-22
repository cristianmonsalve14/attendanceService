package cl.duoc.libroDigital.attendanceService.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AttendanceCatalogService {

    private static final Map<String, Short> SESSION_STATUS_IDS = Map.of(
            "ABIERTA", (short) 1,
            "CERRADA", (short) 2
    );

    private static final Map<Short, String> SESSION_STATUS_CODES = Map.of(
            (short) 1, "ABIERTA",
            (short) 2, "CERRADA"
    );

    private static final Map<String, Short> ATTENDANCE_STATUS_IDS = Map.of(
            "PRESENTE", (short) 1,
            "AUSENTE", (short) 2,
            "ATRASADO", (short) 3,
            "JUSTIFICADO", (short) 4
    );

    private static final Map<Short, String> ATTENDANCE_STATUS_CODES = Map.of(
            (short) 1, "PRESENTE",
            (short) 2, "AUSENTE",
            (short) 3, "ATRASADO",
            (short) 4, "JUSTIFICADO"
    );

    private static final Map<String, Short> ANNOTATION_TYPE_IDS = Map.of(
            "POSITIVA", (short) 1,
            "NEGATIVA", (short) 2
    );

    private static final Map<Short, String> ANNOTATION_TYPE_CODES = Map.of(
            (short) 1, "POSITIVA",
            (short) 2, "NEGATIVA"
    );

    public Short sessionStatusId(String code) {
        if (code == null || code.isBlank()) {
            return (short) 1;
        }
        return SESSION_STATUS_IDS.getOrDefault(code.trim().toUpperCase(), (short) 1);
    }

    public String sessionStatusCode(Short id) {
        if (id == null) {
            return "ABIERTA";
        }
        return SESSION_STATUS_CODES.getOrDefault(id, "ABIERTA");
    }

    public Short attendanceStatusId(String code) {
        if (code == null || code.isBlank()) {
            return (short) 1;
        }
        return ATTENDANCE_STATUS_IDS.getOrDefault(code.trim().toUpperCase(), (short) 1);
    }

    public String attendanceStatusCode(Short id) {
        if (id == null) {
            return "PRESENTE";
        }
        return ATTENDANCE_STATUS_CODES.getOrDefault(id, "PRESENTE");
    }

    public Short annotationTypeId(String code) {
        if (code == null || code.isBlank()) {
            return (short) 1;
        }
        return ANNOTATION_TYPE_IDS.getOrDefault(code.trim().toUpperCase(), (short) 1);
    }

    public String annotationTypeCode(Short id) {
        if (id == null) {
            return "POSITIVA";
        }
        return ANNOTATION_TYPE_CODES.getOrDefault(id, "POSITIVA");
    }
}
