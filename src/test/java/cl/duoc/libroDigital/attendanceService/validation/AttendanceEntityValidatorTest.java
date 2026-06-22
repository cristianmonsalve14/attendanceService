package cl.duoc.libroDigital.attendanceService.validation;

import cl.duoc.libroDigital.attendanceService.exception.BadRequestException;
import cl.duoc.libroDigital.attendanceService.exception.ConflictException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.repository.ClassSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceEntityValidatorTest {

    @Mock
    private ClassSessionRepository classSessionRepository;

    @InjectMocks
    private AttendanceEntityValidator validator;

    @Test
    void validateSessionForSave_rejectsMissingSubject() {
        ClassSession session = validSession();
        session.setSubjectId(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("asignatura"));
    }

    @Test
    void validateSessionForSave_rejectsMissingCourse() {
        ClassSession session = validSession();
        session.setCourseId(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("curso"));
    }

    @Test
    void validateSessionForSave_rejectsMissingTeacher() {
        ClassSession session = validSession();
        session.setTeacherId(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("docente"));
    }

    @Test
    void validateSessionForSave_rejectsInvalidTeacherId() {
        ClassSession session = validSession();
        session.setTeacherId(0L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("docente"));
    }

    @Test
    void validateSessionForSave_rejectsMissingSessionDate() {
        ClassSession session = validSession();
        session.setSessionDate(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("fecha"));
    }

    @Test
    void validateSessionForSave_rejectsFutureDate() {
        ClassSession session = validSession();
        session.setSessionDate(LocalDate.now().plusDays(1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("futura"));
    }

    @Test
    void validateSessionForSave_rejectsDuplicateSession() {
        ClassSession session = validSession();
        ClassSession existing = validSession();
        existing.setId(999L);

        when(classSessionRepository.findBySubjectIdAndSessionDate(session.getSubjectId(), session.getSessionDate()))
                .thenReturn(List.of(existing));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> validator.validateSessionForSave(session, null));

        assertTrue(ex.getMessage().contains("Ya existe una sesión"));
    }

    @Test
    void validateSessionForSave_allowsSameSessionWhenExcludedIdMatches() {
        ClassSession session = validSession();
        ClassSession existing = validSession();
        existing.setId(10L);

        when(classSessionRepository.findBySubjectIdAndSessionDate(session.getSubjectId(), session.getSessionDate()))
                .thenReturn(List.of(existing));

        assertDoesNotThrow(() -> validator.validateSessionForSave(session, 10L));
        verify(classSessionRepository).findBySubjectIdAndSessionDate(session.getSubjectId(), session.getSessionDate());
    }

    @Test
    void validateAnnotationForSave_rejectsMissingStudent() {
        Annotation annotation = validAnnotation();
        annotation.setStudentId(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("estudiante"));
    }

    @Test
    void validateAnnotationForSave_rejectsMissingTeacher() {
        Annotation annotation = validAnnotation();
        annotation.setTeacherId(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("docente"));
    }

    @Test
    void validateAnnotationForSave_rejectsInvalidTeacherId() {
        Annotation annotation = validAnnotation();
        annotation.setTeacherId(0L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("docente"));
    }

    @Test
    void validateAnnotationForSave_rejectsMissingDate() {
        Annotation annotation = validAnnotation();
        annotation.setAnnotationDate(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("fecha"));
    }

    @Test
    void validateAnnotationForSave_rejectsFutureDate() {
        Annotation annotation = validAnnotation();
        annotation.setAnnotationDate(LocalDate.now().plusDays(1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("futura"));
    }

    @Test
    void validateAnnotationForSave_rejectsInvalidType() {
        Annotation annotation = validAnnotation();
        annotation.setAnnotationTypeId((short) 99);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("tipo de anotación"));
    }

    @Test
    void validateAnnotationForSave_rejectsMissingType() {
        Annotation annotation = validAnnotation();
        annotation.setAnnotationTypeId(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("tipo de anotación"));
    }

    @Test
    void validateAnnotationForSave_rejectsBlankDescription() {
        Annotation annotation = validAnnotation();
        annotation.setDescription("   ");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateAnnotationForSave(annotation));

        assertTrue(ex.getMessage().contains("descripción"));
    }

    @Test
    void validateAnnotationForSave_trimsDescriptionWhenValid() {
        Annotation annotation = validAnnotation();
        annotation.setDescription("  Observación positiva  ");

        assertDoesNotThrow(() -> validator.validateAnnotationForSave(annotation));
        assertEquals("Observación positiva", annotation.getDescription());
    }

    private static ClassSession validSession() {
        ClassSession session = new ClassSession();
        session.setCourseId(1L);
        session.setSubjectId(2L);
        session.setTeacherId(3L);
        session.setSessionDate(LocalDate.now().minusDays(1));
        return session;
    }

    private static Annotation validAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setStudentId(10L);
        annotation.setTeacherId(20L);
        annotation.setAnnotationDate(LocalDate.now().minusDays(1));
        annotation.setAnnotationTypeId((short) 1);
        annotation.setDescription("Descripción válida");
        return annotation;
    }
}
