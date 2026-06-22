package cl.duoc.libroDigital.attendanceService.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcademicApiClientTest {

    private final AcademicApiClient apiClient = new AcademicApiClient("http://localhost:8092");

    @Test
    void getCurrentTeacherId_returnsEmptyForNullOrBlankHeader() {
        assertTrue(apiClient.getCurrentTeacherId(null).isEmpty());
        assertTrue(apiClient.getCurrentTeacherId("   ").isEmpty());
    }

    @Test
    void getStudentName_returnsEmptyForNullOrBlankHeaderOrNullStudent() {
        assertTrue(apiClient.getStudentName(null, 10L).isEmpty());
        assertTrue(apiClient.getStudentName("   ", 10L).isEmpty());
        assertTrue(apiClient.getStudentName("Bearer token", null).isEmpty());
    }

    @Test
    void getSubjectName_returnsEmptyForNullOrBlankHeaderOrNullSubject() {
        assertTrue(apiClient.getSubjectName(null, 5L).isEmpty());
        assertTrue(apiClient.getSubjectName("   ", 5L).isEmpty());
        assertTrue(apiClient.getSubjectName("Bearer token", null).isEmpty());
    }

    @Test
    void canReadStudent_returnsFalseForNullOrBlankHeaderOrNullStudent() {
        assertFalse(apiClient.canReadStudent(null, 12L));
        assertFalse(apiClient.canReadStudent("   ", 12L));
        assertFalse(apiClient.canReadStudent("Bearer token", null));
    }
}
