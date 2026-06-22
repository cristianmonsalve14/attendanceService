package cl.duoc.libroDigital.attendanceService.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleForbidden() {
        ResponseEntity<Map<String, String>> response =
                handler.handleForbidden(new ForbiddenException("Sin permiso"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Sin permiso", response.getBody().get("message"));
    }

    @Test
    void handleBadRequest() {
        ResponseEntity<Map<String, String>> response =
                handler.handleBadRequest(new BadRequestException("Datos inválidos"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Datos inválidos", response.getBody().get("message"));
    }

    @Test
    void handleConflict() {
        ResponseEntity<Map<String, String>> response =
                handler.handleConflict(new ConflictException("Conflicto de datos"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflicto de datos", response.getBody().get("message"));
    }

    @Test
    void handleNotFound() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new NotFoundException("No encontrado"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No encontrado", response.getBody().get("message"));
    }
}
