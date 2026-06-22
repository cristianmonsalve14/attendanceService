package cl.duoc.libroDigital.attendanceService.controller;

import cl.duoc.libroDigital.attendanceService.dto.AttendanceRecordDTO;
import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.security.AttendanceAccessService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceCatalogService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceRecordService;
import cl.duoc.libroDigital.attendanceService.service.ClassSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AttendanceRecordService attendanceRecordService;
    @Mock
    private ClassSessionService classSessionService;
    @Mock
    private AttendanceAccessService access;
    @Mock
    private AttendanceCatalogService catalogs;

    @InjectMocks
    private AttendanceController attendanceController;

    @Test
    void createAttendance_requiresTeacherAccessAndMapsNames() {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setSessionId(30L);
        dto.setStudentId(200L);
        dto.setStatus("PRESENTE");
        dto.setObservations("Sin novedad");

        AttendanceRecord created = new AttendanceRecord();
        created.setId(7L);
        created.setSessionId(30L);
        created.setStudentId(200L);
        created.setAttendanceStatusId((short) 1);
        created.setObservations("Sin novedad");

        ClassSession session = new ClassSession();
        session.setId(30L);
        session.setSubjectId(44L);
        session.setSessionDate(LocalDate.of(2026, 6, 10));
        session.setTopic("Ecuaciones lineales");

        when(catalogs.attendanceStatusId("PRESENTE")).thenReturn((short) 1);
        when(attendanceRecordService.createAttendance(any(AttendanceRecord.class))).thenReturn(created);
        when(access.resolveStudentName(200L)).thenReturn(Optional.of("Ana Pérez"));
        when(classSessionService.getSessionById(30L)).thenReturn(Optional.of(session));
        when(access.resolveSubjectName(44L)).thenReturn(Optional.of("Matemática"));
        when(catalogs.attendanceStatusCode((short) 1)).thenReturn("PRESENTE");

        AttendanceRecordDTO response = attendanceController.createAttendance(dto);

        assertEquals(7L, response.getId());
        assertEquals("Ana Pérez", response.getStudentName());
        assertEquals("Matemática", response.getSubjectName());
        assertEquals("PRESENTE", response.getStatus());
        verify(access).requireTeacherForPedagogicalWrite();
        verify(access).ensureCanAccessSession(30L);
    }

    @Test
    void getAllAttendances_nonAdminFiltersByTeacherSessions() {
        AttendanceRecord allowed = new AttendanceRecord();
        allowed.setId(1L);
        allowed.setSessionId(10L);
        allowed.setStudentId(500L);
        allowed.setAttendanceStatusId((short) 1);

        AttendanceRecord denied = new AttendanceRecord();
        denied.setId(2L);
        denied.setSessionId(20L);
        denied.setStudentId(501L);
        denied.setAttendanceStatusId((short) 1);

        ClassSession teacherSession = new ClassSession();
        teacherSession.setId(10L);
        teacherSession.setSubjectId(70L);
        teacherSession.setSessionDate(LocalDate.of(2026, 6, 9));
        teacherSession.setTopic("Repaso");

        when(access.isAdmin()).thenReturn(false);
        when(access.requireTeacherId()).thenReturn(9L);
        when(classSessionService.getSessionsByTeacher(9L)).thenReturn(List.of(teacherSession));
        when(attendanceRecordService.getAllAttendances()).thenReturn(List.of(allowed, denied));
        when(access.resolveStudentName(500L)).thenReturn(Optional.of("Alumno Uno"));
        when(classSessionService.getSessionById(10L)).thenReturn(Optional.of(teacherSession));
        when(access.resolveSubjectName(70L)).thenReturn(Optional.of("Historia"));
        when(catalogs.attendanceStatusCode((short) 1)).thenReturn("PRESENTE");

        List<AttendanceRecordDTO> response = attendanceController.getAllAttendances();

        assertEquals(1, response.size());
        assertEquals(1L, response.getFirst().getId());
    }

    @Test
    void getAttendance_whenNotFoundReturnsNull() {
        when(attendanceRecordService.getAttendanceById(404L)).thenReturn(Optional.empty());

        AttendanceRecordDTO response = attendanceController.getAttendance(404L);

        assertNull(response);
    }

    @Test
    void getAttendance_whenFoundChecksSessionAccess() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(3L);
        record.setSessionId(55L);
        record.setStudentId(300L);
        record.setAttendanceStatusId((short) 1);

        ClassSession session = new ClassSession();
        session.setId(55L);
        session.setSubjectId(9L);
        session.setSessionDate(LocalDate.of(2026, 6, 1));
        session.setTopic("Clase");

        when(attendanceRecordService.getAttendanceById(3L)).thenReturn(Optional.of(record));
        when(access.resolveStudentName(300L)).thenReturn(Optional.of("Estudiante"));
        when(classSessionService.getSessionById(55L)).thenReturn(Optional.of(session));
        when(access.resolveSubjectName(9L)).thenReturn(Optional.of("Lenguaje"));
        when(catalogs.attendanceStatusCode((short) 1)).thenReturn("PRESENTE");

        attendanceController.getAttendance(3L);

        verify(access).ensureCanAccessSession(55L);
    }

    @Test
    void getAllAttendances_adminSeesAll() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(8L);
        record.setSessionId(1L);
        record.setStudentId(2L);
        record.setAttendanceStatusId((short) 1);

        when(access.isAdmin()).thenReturn(true);
        when(attendanceRecordService.getAllAttendances()).thenReturn(List.of(record));
        when(access.resolveStudentName(2L)).thenReturn(Optional.empty());
        when(classSessionService.getSessionById(1L)).thenReturn(Optional.empty());
        when(catalogs.attendanceStatusCode((short) 1)).thenReturn("PRESENTE");

        assertEquals(1, attendanceController.getAllAttendances().size());
    }

    @Test
    void getAttendancesBySession_checksAccess() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(4L);
        record.setSessionId(60L);
        record.setStudentId(10L);
        record.setAttendanceStatusId((short) 2);

        when(attendanceRecordService.getAttendancesBySession(60L)).thenReturn(List.of(record));
        when(access.resolveStudentName(10L)).thenReturn(Optional.of("Pedro"));
        when(classSessionService.getSessionById(60L)).thenReturn(Optional.empty());
        when(catalogs.attendanceStatusCode((short) 2)).thenReturn("AUSENTE");

        List<AttendanceRecordDTO> result = attendanceController.getAttendancesBySession(60L);

        assertEquals(1, result.size());
        verify(access).ensureCanAccessSession(60L);
    }

    @Test
    void getAttendancesByStudent_checksStudentAccess() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(5L);
        record.setSessionId(70L);
        record.setStudentId(88L);
        record.setAttendanceStatusId((short) 1);

        when(attendanceRecordService.getAttendancesByStudent(88L)).thenReturn(List.of(record));
        when(access.resolveStudentName(88L)).thenReturn(Optional.of("María"));
        when(classSessionService.getSessionById(70L)).thenReturn(Optional.empty());
        when(catalogs.attendanceStatusCode((short) 1)).thenReturn("PRESENTE");

        attendanceController.getAttendancesByStudent(88L);

        verify(access).ensureCanReadStudent(88L);
    }

    @Test
    void updateAndDeleteAttendance_checkAccess() {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setSessionId(11L);
        dto.setStudentId(22L);
        dto.setStatus("PRESENTE");

        AttendanceRecord updated = new AttendanceRecord();
        updated.setId(6L);
        updated.setSessionId(11L);
        updated.setStudentId(22L);
        updated.setAttendanceStatusId((short) 1);

        when(catalogs.attendanceStatusId("PRESENTE")).thenReturn((short) 1);
        when(attendanceRecordService.updateAttendance(eq(6L), any(AttendanceRecord.class))).thenReturn(updated);
        when(access.resolveStudentName(22L)).thenReturn(Optional.empty());
        when(classSessionService.getSessionById(11L)).thenReturn(Optional.empty());
        when(catalogs.attendanceStatusCode((short) 1)).thenReturn("PRESENTE");

        attendanceController.updateAttendance(6L, dto);
        attendanceController.deleteAttendance(6L);

        verify(access, times(2)).ensureCanManageAttendance(6L);
        verify(access).ensureCanAccessSession(11L);
        verify(attendanceRecordService).deleteAttendance(6L);
    }
}
