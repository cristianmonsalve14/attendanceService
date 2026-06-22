package cl.duoc.libroDigital.attendanceService.service;

import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.repository.ClassSessionRepository;
import cl.duoc.libroDigital.attendanceService.service.impl.ClassSessionServiceImpl;
import cl.duoc.libroDigital.attendanceService.validation.AttendanceEntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassSessionServiceImplTest {

    @Mock
    private ClassSessionRepository classSessionRepository;

    @Mock
    private AttendanceEntityValidator validator;

    @InjectMocks
    private ClassSessionServiceImpl classSessionService;

    @Test
    void createSession_delegatesToRepository() {
        ClassSession session = new ClassSession();
        when(classSessionRepository.save(session)).thenReturn(session);

        assertSame(session, classSessionService.createSession(session));
        verify(validator).validateSessionForSave(session, null);
    }

    @Test
    void getSessionsByTeacher() {
        when(classSessionRepository.findByTeacherId(3L)).thenReturn(List.of(new ClassSession()));
        assertEquals(1, classSessionService.getSessionsByTeacher(3L).size());
    }

    @Test
    void updateSession_notFound() {
        when(classSessionRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> classSessionService.updateSession(9L, new ClassSession()));
    }

    @Test
    void deleteSession() {
        classSessionService.deleteSession(4L);
        verify(classSessionRepository).deleteById(4L);
    }
}
