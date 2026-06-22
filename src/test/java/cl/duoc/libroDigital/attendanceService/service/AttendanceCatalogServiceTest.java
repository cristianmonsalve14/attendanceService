package cl.duoc.libroDigital.attendanceService.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceCatalogServiceTest {

    private final AttendanceCatalogService catalogs = new AttendanceCatalogService();

    @Test
    void sessionStatusMappings() {
        assertEquals((short) 1, catalogs.sessionStatusId("ABIERTA"));
        assertEquals((short) 2, catalogs.sessionStatusId("CERRADA"));
        assertEquals("ABIERTA", catalogs.sessionStatusCode((short) 1));
        assertEquals("ABIERTA", catalogs.sessionStatusCode(null));
    }

    @Test
    void attendanceStatusMappings() {
        assertEquals((short) 2, catalogs.attendanceStatusId("AUSENTE"));
        assertEquals("JUSTIFICADO", catalogs.attendanceStatusCode((short) 4));
        assertEquals((short) 1, catalogs.attendanceStatusId(null));
    }

    @Test
    void annotationTypeMappings() {
        assertEquals((short) 2, catalogs.annotationTypeId("NEGATIVA"));
        assertEquals("POSITIVA", catalogs.annotationTypeCode((short) 1));
        assertEquals("POSITIVA", catalogs.annotationTypeCode(null));
    }
}
