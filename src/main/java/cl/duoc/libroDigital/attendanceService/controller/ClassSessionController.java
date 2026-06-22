package cl.duoc.libroDigital.attendanceService.controller;

import cl.duoc.libroDigital.attendanceService.dto.ClassSessionDTO;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.security.AttendanceAccessService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceCatalogService;
import cl.duoc.libroDigital.attendanceService.service.ClassSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sessions")
public class ClassSessionController {

    private final ClassSessionService classSessionService;
    private final AttendanceAccessService access;
    private final AttendanceCatalogService catalogs;

    public ClassSessionController(
            ClassSessionService classSessionService,
            AttendanceAccessService access,
            AttendanceCatalogService catalogs) {
        this.classSessionService = classSessionService;
        this.access = access;
        this.catalogs = catalogs;
    }

    private ClassSessionDTO toDTO(ClassSession session) {
        ClassSessionDTO dto = new ClassSessionDTO();
        dto.setId(session.getId());
        dto.setCourseId(session.getCourseId());
        dto.setSubjectId(session.getSubjectId());
        dto.setTeacherId(session.getTeacherId());
        dto.setSessionDate(session.getSessionDate());
        dto.setTopic(session.getTopic());
        dto.setSessionStatus(catalogs.sessionStatusCode(session.getSessionStatusId()));
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
    }

    private ClassSession toEntity(ClassSessionDTO dto) {
        ClassSession session = new ClassSession();
        session.setId(dto.getId());
        session.setCourseId(dto.getCourseId());
        session.setSubjectId(dto.getSubjectId());
        session.setTeacherId(dto.getTeacherId());
        session.setSessionDate(dto.getSessionDate());
        session.setTopic(dto.getTopic());
        session.setSessionStatusId(catalogs.sessionStatusId(dto.getSessionStatus()));
        return session;
    }

    @PostMapping
    public ClassSessionDTO createSession(@RequestBody ClassSessionDTO dto) {
        ClassSession entity = toEntity(dto);
        if (access.isTeacher() && !access.isAdmin()) {
            entity.setTeacherId(access.requireTeacherId());
        }
        access.ensureCanManageSession(entity);
        return toDTO(classSessionService.createSession(entity));
    }

    @GetMapping
    public List<ClassSessionDTO> getAllSessions() {
        if (access.isAdmin()) {
            return classSessionService.getAllSessions()
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        Long teacherId = access.requireTeacherId();
        return classSessionService.getSessionsByTeacher(teacherId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClassSessionDTO getSession(@PathVariable Long id) {
        access.ensureCanAccessSession(id);
        return classSessionService.getSessionById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @GetMapping("/course/{courseId}")
    public List<ClassSessionDTO> getSessionsByCourse(@PathVariable Long courseId) {
        List<ClassSession> sessions = classSessionService.getSessionsByCourse(courseId);
        if (!access.isAdmin()) {
            Long teacherId = access.requireTeacherId();
            sessions = sessions.stream()
                    .filter(session -> teacherId.equals(session.getTeacherId()))
                    .collect(Collectors.toList());
        }
        return sessions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/subject/{subjectId}")
    public List<ClassSessionDTO> getSessionsBySubject(@PathVariable Long subjectId) {
        List<ClassSession> sessions = classSessionService.getSessionsBySubject(subjectId);
        if (!access.isAdmin()) {
            Long teacherId = access.requireTeacherId();
            sessions = sessions.stream()
                    .filter(session -> teacherId.equals(session.getTeacherId()))
                    .collect(Collectors.toList());
        }
        return sessions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ClassSessionDTO updateSession(@PathVariable Long id, @RequestBody ClassSessionDTO dto) {
        access.ensureCanAccessSession(id);
        ClassSession entity = toEntity(dto);
        if (access.isTeacher() && !access.isAdmin()) {
            entity.setTeacherId(access.requireTeacherId());
        }
        access.ensureCanManageSession(entity);
        return toDTO(classSessionService.updateSession(id, entity));
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable Long id) {
        access.requireTeacherForPedagogicalWrite();
        access.ensureCanAccessSession(id);
        classSessionService.deleteSession(id);
    }
}
