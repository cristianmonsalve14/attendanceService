package cl.duoc.libroDigital.attendanceService.service;

import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;
import cl.duoc.libroDigital.attendanceService.repository.AttendanceRecordRepository;
import cl.duoc.libroDigital.attendanceService.service.impl.AttendanceRecordServiceImpl;
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
class AttendanceRecordServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @InjectMocks
    private AttendanceRecordServiceImpl attendanceRecordService;

    @Test
    void createAttendance_success() {
        AttendanceRecord record = new AttendanceRecord();
        record.setSessionId(1L);
        record.setStudentId(2L);
        record.setAttendanceStatusId((short) 1);

        when(attendanceRecordRepository.findBySessionIdAndStudentId(1L, 2L)).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(record)).thenReturn(record);

        AttendanceRecord saved = attendanceRecordService.createAttendance(record);
        assertSame(record, saved);
    }

    @Test
    void createAttendance_rejectsDuplicate() {
        AttendanceRecord record = new AttendanceRecord();
        record.setSessionId(1L);
        record.setStudentId(2L);
        record.setAttendanceStatusId((short) 1);

        when(attendanceRecordRepository.findBySessionIdAndStudentId(1L, 2L))
                .thenReturn(Optional.of(new AttendanceRecord()));

        assertThrows(RuntimeException.class, () -> attendanceRecordService.createAttendance(record));
    }

    @Test
    void createAttendance_rejectsInvalidStatus() {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceStatusId((short) 99);

        assertThrows(RuntimeException.class, () -> attendanceRecordService.createAttendance(record));
    }

    @Test
    void getAttendancesByStudent() {
        when(attendanceRecordRepository.findByStudentId(2L)).thenReturn(List.of(new AttendanceRecord()));
        assertEquals(1, attendanceRecordService.getAttendancesByStudent(2L).size());
    }

    @Test
    void updateAttendance_success() {
        AttendanceRecord existing = new AttendanceRecord();
        existing.setId(1L);
        existing.setSessionId(10L);
        existing.setStudentId(20L);
        existing.setAttendanceStatusId((short) 1);

        AttendanceRecord patch = new AttendanceRecord();
        patch.setAttendanceStatusId((short) 2);
        patch.setObservations("Llegó tarde");

        when(attendanceRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(attendanceRecordRepository.save(existing)).thenReturn(existing);

        AttendanceRecord result = attendanceRecordService.updateAttendance(1L, patch);

        assertEquals((short) 2, result.getAttendanceStatusId());
        assertEquals("Llegó tarde", result.getObservations());
    }

    @Test
    void getAttendancesBySession() {
        when(attendanceRecordRepository.findBySessionId(5L)).thenReturn(List.of(new AttendanceRecord()));
        assertEquals(1, attendanceRecordService.getAttendancesBySession(5L).size());
    }

    @Test
    void getAttendanceById() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(3L);
        when(attendanceRecordRepository.findById(3L)).thenReturn(Optional.of(record));
        assertTrue(attendanceRecordService.getAttendanceById(3L).isPresent());
    }

    @Test
    void getAllAttendances() {
        when(attendanceRecordRepository.findAll()).thenReturn(List.of(new AttendanceRecord()));
        assertEquals(1, attendanceRecordService.getAllAttendances().size());
    }

    @Test
    void updateAttendance_notFound() {
        when(attendanceRecordRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> attendanceRecordService.updateAttendance(99L, new AttendanceRecord()));
    }

    @Test
    void deleteAttendance() {
        attendanceRecordService.deleteAttendance(5L);
        verify(attendanceRecordRepository).deleteById(5L);
    }
}
