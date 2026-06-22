package cl.duoc.libroDigital.attendanceService.service;

import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordService {

    AttendanceRecord createAttendance(AttendanceRecord record);

    List<AttendanceRecord> getAllAttendances();

    Optional<AttendanceRecord> getAttendanceById(Long id);

    AttendanceRecord updateAttendance(Long id, AttendanceRecord record);

    void deleteAttendance(Long id);

    List<AttendanceRecord> getAttendancesBySession(Long sessionId);

    List<AttendanceRecord> getAttendancesByStudent(Long studentId);
}
