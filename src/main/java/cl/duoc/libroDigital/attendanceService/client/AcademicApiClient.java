package cl.duoc.libroDigital.attendanceService.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Component
public class AcademicApiClient {

    private final RestClient restClient;
    private final String academicBaseUrl;

    public AcademicApiClient(@Value("${academic.service.url:http://localhost:8092}") String academicBaseUrl) {
        this.restClient = RestClient.create();
        this.academicBaseUrl = academicBaseUrl.replaceAll("/$", "");
    }

    public Optional<Long> getCurrentTeacherId(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(academicBaseUrl + "/teachers/me")
                    .header("Authorization", authorizationHeader)
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("id") == null) {
                return Optional.empty();
            }
            return Optional.of(((Number) body.get("id")).longValue());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getStudentName(String authorizationHeader, Long studentId) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || studentId == null) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(academicBaseUrl + "/students/{id}", studentId)
                    .header("Authorization", authorizationHeader)
                    .retrieve()
                    .body(Map.class);
            if (body == null) {
                return Optional.empty();
            }
            String firstName = body.get("firstName") != null ? body.get("firstName").toString().trim() : "";
            String lastName = body.get("lastName") != null ? body.get("lastName").toString().trim() : "";
            String fullName = (firstName + " " + lastName).trim();
            return fullName.isBlank() ? Optional.empty() : Optional.of(fullName);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getSubjectName(String authorizationHeader, Long subjectId) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || subjectId == null) {
            return Optional.empty();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(academicBaseUrl + "/subjects/{id}", subjectId)
                    .header("Authorization", authorizationHeader)
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("subjectName") == null) {
                return Optional.empty();
            }
            String name = body.get("subjectName").toString().trim();
            return name.isBlank() ? Optional.empty() : Optional.of(name);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public boolean canReadStudent(String authorizationHeader, Long studentId) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || studentId == null) {
            return false;
        }
        try {
            restClient.get()
                    .uri(academicBaseUrl + "/students/{id}", studentId)
                    .header("Authorization", authorizationHeader)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
