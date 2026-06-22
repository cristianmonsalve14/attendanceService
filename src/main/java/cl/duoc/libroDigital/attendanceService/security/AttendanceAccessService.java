package cl.duoc.libroDigital.attendanceService.security;

import cl.duoc.libroDigital.attendanceService.client.AcademicApiClient;
import cl.duoc.libroDigital.attendanceService.exception.ForbiddenException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.model.AttendanceRecord;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.repository.AnnotationRepository;
import cl.duoc.libroDigital.attendanceService.repository.AttendanceRecordRepository;
import cl.duoc.libroDigital.attendanceService.repository.ClassSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
public class AttendanceAccessService {

    private final ClassSessionRepository classSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AnnotationRepository annotationRepository;
    private final AcademicApiClient academicApiClient;

    public AttendanceAccessService(
            ClassSessionRepository classSessionRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            AnnotationRepository annotationRepository,
            AcademicApiClient academicApiClient) {
        this.classSessionRepository = classSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.annotationRepository = annotationRepository;
        this.academicApiClient = academicApiClient;
    }

    public boolean isAdmin() {
        return hasRole("ADMINISTRADOR");
    }

    public boolean isTeacher() {
        return hasRole("DOCENTE");
    }

    public boolean isGuardian() {
        return hasRole("APODERADO");
    }

    public boolean isStudent() {
        return hasRole("ESTUDIANTE");
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new ForbiddenException("Solo administración puede realizar esta acción");
        }
    }

    public void requireTeacherForPedagogicalWrite() {
        if (isAdmin()) {
            throw new ForbiddenException("La administración solo puede consultar asistencia y anotaciones, no modificarlas");
        }
        if (!isTeacher()) {
            throw new ForbiddenException("No tiene permisos para esta acción");
        }
    }

    public Long requireTeacherId() {
        return currentTeacherId()
                .orElseThrow(() -> new ForbiddenException("No hay un profesor vinculado a esta cuenta"));
    }

    public Optional<Long> currentTeacherId() {
        if (isAdmin()) {
            return Optional.empty();
        }
        return academicApiClient.getCurrentTeacherId(resolveAuthorizationHeader());
    }

    public void ensureCanReadStudent(Long studentId) {
        if (isAdmin()) {
            return;
        }
        if (!academicApiClient.canReadStudent(resolveAuthorizationHeader(), studentId)) {
            throw new ForbiddenException("No puede acceder a este estudiante");
        }
    }

    public void ensureCanAccessSession(Long sessionId) {
        if (isAdmin()) {
            return;
        }
        Long teacherId = requireTeacherId();
        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ForbiddenException("Sesión no encontrada"));
        if (!teacherId.equals(session.getTeacherId())) {
            throw new ForbiddenException("No puede acceder a esta sesión");
        }
    }

    public void ensureCanManageSession(ClassSession session) {
        requireTeacherForPedagogicalWrite();
        Long teacherId = requireTeacherId();
        if (session.getTeacherId() != null && !teacherId.equals(session.getTeacherId())) {
            throw new ForbiddenException("No puede gestionar esta sesión");
        }
    }

    public void ensureCanManageAttendance(Long attendanceId) {
        requireTeacherForPedagogicalWrite();
        AttendanceRecord record = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new ForbiddenException("Registro de asistencia no encontrado"));
        ensureCanAccessSession(record.getSessionId());
    }

    public void ensureCanManageAnnotation(Long annotationId) {
        requireTeacherForPedagogicalWrite();
        Long teacherId = requireTeacherId();
        Annotation annotation = annotationRepository.findById(annotationId)
                .orElseThrow(() -> new ForbiddenException("Anotación no encontrada"));
        if (!teacherId.equals(annotation.getTeacherId())) {
            throw new ForbiddenException("No puede gestionar esta anotación");
        }
    }

    public String resolveAuthorizationHeader() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader("Authorization");
    }

    public Optional<String> resolveStudentName(Long studentId) {
        return academicApiClient.getStudentName(resolveAuthorizationHeader(), studentId);
    }

    public Optional<String> resolveSubjectName(Long subjectId) {
        return academicApiClient.getSubjectName(resolveAuthorizationHeader(), subjectId);
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        String roleAuthority = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(roleAuthority) || authority.equals(role));
    }
}
