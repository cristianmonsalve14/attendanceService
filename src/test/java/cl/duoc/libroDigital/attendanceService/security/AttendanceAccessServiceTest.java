package cl.duoc.libroDigital.attendanceService.security;

import cl.duoc.libroDigital.attendanceService.client.AcademicApiClient;
import cl.duoc.libroDigital.attendanceService.exception.ForbiddenException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.repository.AnnotationRepository;
import cl.duoc.libroDigital.attendanceService.repository.AttendanceRecordRepository;
import cl.duoc.libroDigital.attendanceService.repository.ClassSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceAccessServiceTest {

    @Mock
    private ClassSessionRepository classSessionRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private AnnotationRepository annotationRepository;

    @Mock
    private AcademicApiClient academicApiClient;

    @InjectMocks
    private AttendanceAccessService accessService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void roleChecks() {
        authenticate("ADMINISTRADOR");
        assertTrue(accessService.isAdmin());
        assertFalse(accessService.isTeacher());

        authenticate("DOCENTE");
        assertTrue(accessService.isTeacher());
        assertFalse(accessService.isAdmin());
    }

    @Test
    void requireAdmin_throwsForTeacher() {
        authenticate("DOCENTE");
        assertThrows(ForbiddenException.class, () -> accessService.requireAdmin());
    }

    @Test
    void requireTeacherForPedagogicalWrite_adminCannotWrite() {
        authenticate("ADMINISTRADOR");
        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> accessService.requireTeacherForPedagogicalWrite());
        assertTrue(ex.getMessage().contains("administración"));
    }

    @Test
    void ensureCanReadStudent_adminAlwaysAllowed() {
        authenticate("ADMINISTRADOR");
        assertDoesNotThrow(() -> accessService.ensureCanReadStudent(99L));
        verifyNoInteractions(academicApiClient);
    }

    @Test
    void ensureCanReadStudent_deniedWhenApiFails() {
        authenticate("APODERADO");
        when(academicApiClient.canReadStudent(null, 2L)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> accessService.ensureCanReadStudent(2L));
    }

    @Test
    void ensureCanAccessSession_teacherOwnsSession() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));
        ClassSession session = new ClassSession();
        session.setTeacherId(7L);
        when(classSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> accessService.ensureCanAccessSession(1L));
    }

    @Test
    void ensureCanManageAnnotation_wrongTeacher() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));
        Annotation annotation = new Annotation();
        annotation.setTeacherId(8L);
        when(annotationRepository.findById(5L)).thenReturn(Optional.of(annotation));

        assertThrows(ForbiddenException.class, () -> accessService.ensureCanManageAnnotation(5L));
    }

    @Test
    void ensureCanManageAttendance_loadsRecordAndSession() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));
        AttendanceRecord record = new AttendanceRecord();
        record.setSessionId(3L);
        when(attendanceRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        ClassSession session = new ClassSession();
        session.setTeacherId(7L);
        when(classSessionRepository.findById(3L)).thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> accessService.ensureCanManageAttendance(10L));
    }

    @Test
    void isGuardianAndStudent() {
        authenticate("APODERADO");
        assertTrue(accessService.isGuardian());
        assertFalse(accessService.isStudent());

        authenticate("ESTUDIANTE");
        assertTrue(accessService.isStudent());
        assertFalse(accessService.isGuardian());
    }

    @Test
    void requireTeacherId_throwsWhenNotLinked() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> accessService.requireTeacherId());
    }

    @Test
    void currentTeacherId_adminReturnsEmpty() {
        authenticate("ADMINISTRADOR");
        assertTrue(accessService.currentTeacherId().isEmpty());
        verifyNoInteractions(academicApiClient);
    }

    @Test
    void ensureCanReadStudent_allowedWhenApiSucceeds() {
        authenticate("APODERADO");
        when(academicApiClient.canReadStudent(null, 5L)).thenReturn(true);

        assertDoesNotThrow(() -> accessService.ensureCanReadStudent(5L));
    }

    @Test
    void ensureCanAccessSession_wrongTeacherThrows() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));
        ClassSession session = new ClassSession();
        session.setTeacherId(99L);
        when(classSessionRepository.findById(2L)).thenReturn(Optional.of(session));

        assertThrows(ForbiddenException.class, () -> accessService.ensureCanAccessSession(2L));
    }

    @Test
    void ensureCanAccessSession_notFoundThrows() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));
        when(classSessionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> accessService.ensureCanAccessSession(404L));
    }

    @Test
    void ensureCanManageSession_successAndWrongTeacher() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));

        ClassSession own = new ClassSession();
        own.setTeacherId(7L);
        assertDoesNotThrow(() -> accessService.ensureCanManageSession(own));

        ClassSession foreign = new ClassSession();
        foreign.setTeacherId(8L);
        assertThrows(ForbiddenException.class, () -> accessService.ensureCanManageSession(foreign));
    }

    @Test
    void ensureCanManageAnnotation_successForOwnAnnotation() {
        authenticate("DOCENTE");
        when(academicApiClient.getCurrentTeacherId(null)).thenReturn(Optional.of(7L));
        Annotation annotation = new Annotation();
        annotation.setTeacherId(7L);
        when(annotationRepository.findById(12L)).thenReturn(Optional.of(annotation));

        assertDoesNotThrow(() -> accessService.ensureCanManageAnnotation(12L));
    }

    @Test
    void resolveStudentAndSubjectName_delegateToClient() {
        when(academicApiClient.getStudentName(null, 3L)).thenReturn(Optional.of("Juan Pérez"));
        when(academicApiClient.getSubjectName(null, 4L)).thenReturn(Optional.of("Matemática"));

        assertEquals(Optional.of("Juan Pérez"), accessService.resolveStudentName(3L));
        assertEquals(Optional.of("Matemática"), accessService.resolveSubjectName(4L));
    }
}
