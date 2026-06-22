package cl.duoc.libroDigital.attendanceService.controller;

import cl.duoc.libroDigital.attendanceService.dto.ClassSessionDTO;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.security.AttendanceAccessService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceCatalogService;
import cl.duoc.libroDigital.attendanceService.service.ClassSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassSessionControllerTest {

    @Mock
    private ClassSessionService classSessionService;
    @Mock
    private AttendanceAccessService access;
    @Mock
    private AttendanceCatalogService catalogs;

    @InjectMocks
    private ClassSessionController classSessionController;

    @Test
    void createSession_teacherOverridesTeacherIdAndValidatesAccess() {
        ClassSessionDTO dto = new ClassSessionDTO();
        dto.setCourseId(5L);
        dto.setSubjectId(9L);
        dto.setTeacherId(999L);
        dto.setSessionDate(LocalDate.of(2026, 6, 10));
        dto.setSessionStatus("ABIERTA");

        ClassSession created = new ClassSession();
        created.setId(70L);
        created.setCourseId(5L);
        created.setSubjectId(9L);
        created.setTeacherId(77L);
        created.setSessionStatusId((short) 1);

        when(catalogs.sessionStatusId("ABIERTA")).thenReturn((short) 1);
        when(access.isTeacher()).thenReturn(true);
        when(access.isAdmin()).thenReturn(false);
        when(access.requireTeacherId()).thenReturn(77L);
        when(classSessionService.createSession(any(ClassSession.class))).thenReturn(created);
        when(catalogs.sessionStatusCode((short) 1)).thenReturn("ABIERTA");

        ClassSessionDTO response = classSessionController.createSession(dto);

        assertEquals(70L, response.getId());
        assertEquals(77L, response.getTeacherId());

        ArgumentCaptor<ClassSession> accessCaptor = ArgumentCaptor.forClass(ClassSession.class);
        verify(access).ensureCanManageSession(accessCaptor.capture());
        assertEquals(77L, accessCaptor.getValue().getTeacherId());
    }

    @Test
    void getAllSessions_nonAdminUsesTeacherSessions() {
        ClassSession session = new ClassSession();
        session.setId(10L);
        session.setTeacherId(2L);
        session.setSessionStatusId((short) 1);

        when(access.isAdmin()).thenReturn(false);
        when(access.requireTeacherId()).thenReturn(2L);
        when(classSessionService.getSessionsByTeacher(2L)).thenReturn(List.of(session));
        when(catalogs.sessionStatusCode((short) 1)).thenReturn("ABIERTA");

        List<ClassSessionDTO> response = classSessionController.getAllSessions();

        assertEquals(1, response.size());
        assertEquals(10L, response.getFirst().getId());
    }

    @Test
    void getSessionsByCourse_nonAdminFiltersOtherTeachers() {
        ClassSession mine = new ClassSession();
        mine.setId(1L);
        mine.setTeacherId(7L);
        mine.setSessionStatusId((short) 1);

        ClassSession other = new ClassSession();
        other.setId(2L);
        other.setTeacherId(8L);
        other.setSessionStatusId((short) 1);

        when(classSessionService.getSessionsByCourse(15L)).thenReturn(List.of(mine, other));
        when(access.isAdmin()).thenReturn(false);
        when(access.requireTeacherId()).thenReturn(7L);
        when(catalogs.sessionStatusCode((short) 1)).thenReturn("ABIERTA");

        List<ClassSessionDTO> response = classSessionController.getSessionsByCourse(15L);

        assertEquals(1, response.size());
        assertEquals(1L, response.getFirst().getId());
    }
}
