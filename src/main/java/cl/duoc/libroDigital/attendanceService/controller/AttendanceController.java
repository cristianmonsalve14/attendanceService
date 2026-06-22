package cl.duoc.libroDigital.attendanceService.controller;

import cl.duoc.libroDigital.attendanceService.dto.AttendanceRecordDTO;
import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;
import cl.duoc.libroDigital.attendanceService.security.AttendanceAccessService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceCatalogService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceRecordService;
import cl.duoc.libroDigital.attendanceService.service.ClassSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceRecordService attendanceRecordService;
    private final ClassSessionService classSessionService;
    private final AttendanceAccessService access;
    private final AttendanceCatalogService catalogs;

    public AttendanceController(
            AttendanceRecordService attendanceRecordService,
            ClassSessionService classSessionService,
            AttendanceAccessService access,
            AttendanceCatalogService catalogs) {
        this.attendanceRecordService = attendanceRecordService;
        this.classSessionService = classSessionService;
        this.access = access;
        this.catalogs = catalogs;
    }

    private AttendanceRecordDTO toDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setSessionId(record.getSessionId());
        dto.setStudentId(record.getStudentId());
        access.resolveStudentName(record.getStudentId()).ifPresent(dto::setStudentName);
        classSessionService.getSessionById(record.getSessionId()).ifPresent(session -> {
            dto.setSessionDate(session.getSessionDate());
            dto.setTopic(session.getTopic());
            access.resolveSubjectName(session.getSubjectId()).ifPresent(dto::setSubjectName);
        });
        dto.setStatus(catalogs.attendanceStatusCode(record.getAttendanceStatusId()));
        dto.setObservations(record.getObservations());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }

    private AttendanceRecord toEntity(AttendanceRecordDTO dto) {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(dto.getId());
        record.setSessionId(dto.getSessionId());
        record.setStudentId(dto.getStudentId());
        record.setAttendanceStatusId(catalogs.attendanceStatusId(dto.getStatus()));
        record.setObservations(dto.getObservations());
        return record;
    }

    @PostMapping
    public AttendanceRecordDTO createAttendance(@RequestBody AttendanceRecordDTO dto) {
        access.requireTeacherForPedagogicalWrite();
        access.ensureCanAccessSession(dto.getSessionId());
        return toDTO(attendanceRecordService.createAttendance(toEntity(dto)));
    }

    @GetMapping
    public List<AttendanceRecordDTO> getAllAttendances() {
        if (access.isAdmin()) {
            return attendanceRecordService.getAllAttendances()
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        Long teacherId = access.requireTeacherId();
        Set<Long> sessionIds = classSessionService.getSessionsByTeacher(teacherId).stream()
                .map(session -> session.getId())
                .collect(Collectors.toSet());
        return attendanceRecordService.getAllAttendances().stream()
                .filter(record -> sessionIds.contains(record.getSessionId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AttendanceRecordDTO getAttendance(@PathVariable Long id) {
        AttendanceRecord record = attendanceRecordService.getAttendanceById(id)
                .orElse(null);
        if (record == null) {
            return null;
        }
        access.ensureCanAccessSession(record.getSessionId());
        return toDTO(record);
    }

    @GetMapping("/session/{sessionId}")
    public List<AttendanceRecordDTO> getAttendancesBySession(@PathVariable Long sessionId) {
        access.ensureCanAccessSession(sessionId);
        return attendanceRecordService.getAttendancesBySession(sessionId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/student/{studentId}")
    public List<AttendanceRecordDTO> getAttendancesByStudent(@PathVariable Long studentId) {
        access.ensureCanReadStudent(studentId);
        return attendanceRecordService.getAttendancesByStudent(studentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public AttendanceRecordDTO updateAttendance(@PathVariable Long id, @RequestBody AttendanceRecordDTO dto) {
        access.ensureCanManageAttendance(id);
        access.ensureCanAccessSession(dto.getSessionId());
        return toDTO(attendanceRecordService.updateAttendance(id, toEntity(dto)));
    }

    @DeleteMapping("/{id}")
    public void deleteAttendance(@PathVariable Long id) {
        access.ensureCanManageAttendance(id);
        attendanceRecordService.deleteAttendance(id);
    }
}
