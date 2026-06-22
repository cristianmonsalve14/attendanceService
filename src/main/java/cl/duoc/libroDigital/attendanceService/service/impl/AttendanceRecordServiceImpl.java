package cl.duoc.libroDigital.attendanceService.service.impl;

import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;
import cl.duoc.libroDigital.attendanceService.repository.AttendanceRecordRepository;
import cl.duoc.libroDigital.attendanceService.service.AttendanceRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private static final Set<Short> VALID_STATUS_IDS = Set.of((short) 1, (short) 2, (short) 3, (short) 4);

    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceRecordServiceImpl(AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Override
    public AttendanceRecord createAttendance(AttendanceRecord record) {
        validateStatusId(record.getAttendanceStatusId());
        attendanceRecordRepository
                .findBySessionIdAndStudentId(record.getSessionId(), record.getStudentId())
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "Ya existe asistencia para el estudiante " + record.getStudentId()
                                    + " en la sesión " + record.getSessionId()
                    );
                });
        return attendanceRecordRepository.save(record);
    }

    @Override
    public List<AttendanceRecord> getAllAttendances() {
        return attendanceRecordRepository.findAll();
    }

    @Override
    public Optional<AttendanceRecord> getAttendanceById(Long id) {
        return attendanceRecordRepository.findById(id);
    }

    @Override
    public AttendanceRecord updateAttendance(Long id, AttendanceRecord record) {
        return attendanceRecordRepository.findById(id).map(existing -> {
            if (record.getSessionId() != null) {
                existing.setSessionId(record.getSessionId());
            }
            if (record.getStudentId() != null) {
                existing.setStudentId(record.getStudentId());
            }
            if (record.getAttendanceStatusId() != null) {
                validateStatusId(record.getAttendanceStatusId());
                existing.setAttendanceStatusId(record.getAttendanceStatusId());
            }
            if (record.getObservations() != null) {
                existing.setObservations(record.getObservations());
            }
            return attendanceRecordRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Asistencia no encontrada con id " + id));
    }

    @Override
    public void deleteAttendance(Long id) {
        attendanceRecordRepository.deleteById(id);
    }

    @Override
    public List<AttendanceRecord> getAttendancesBySession(Long sessionId) {
        return attendanceRecordRepository.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> getAttendancesByStudent(Long studentId) {
        return attendanceRecordRepository.findByStudentId(studentId);
    }

    private void validateStatusId(Short statusId) {
        if (statusId != null && !VALID_STATUS_IDS.contains(statusId)) {
            throw new RuntimeException(
                    "Estado de asistencia inválido. Valores permitidos: PRESENTE, AUSENTE, ATRASADO, JUSTIFICADO"
            );
        }
    }
}
