package cl.duoc.libroDigital.attendanceService.service.impl;

import cl.duoc.libroDigital.attendanceService.exception.NotFoundException;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.repository.ClassSessionRepository;
import cl.duoc.libroDigital.attendanceService.service.ClassSessionService;
import cl.duoc.libroDigital.attendanceService.validation.AttendanceEntityValidator;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassSessionServiceImpl implements ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final AttendanceEntityValidator validator;

    public ClassSessionServiceImpl(ClassSessionRepository classSessionRepository, AttendanceEntityValidator validator) {
        this.classSessionRepository = classSessionRepository;
        this.validator = validator;
    }

    @Override
    public ClassSession createSession(ClassSession session) {
        validator.validateSessionForSave(session, null);
        return classSessionRepository.save(session);
    }

    @Override
    public List<ClassSession> getAllSessions() {
        return classSessionRepository.findAll();
    }

    @Override
    public Optional<ClassSession> getSessionById(Long id) {
        return classSessionRepository.findById(id);
    }

    @Override
    public ClassSession updateSession(Long id, ClassSession session) {
        return classSessionRepository.findById(id).map(existing -> {
            if (session.getCourseId() != null) {
                existing.setCourseId(session.getCourseId());
            }
            if (session.getSubjectId() != null) {
                existing.setSubjectId(session.getSubjectId());
            }
            if (session.getTeacherId() != null) {
                existing.setTeacherId(session.getTeacherId());
            }
            if (session.getSessionDate() != null) {
                existing.setSessionDate(session.getSessionDate());
            }
            if (session.getTopic() != null) {
                existing.setTopic(session.getTopic());
            }
            if (session.getSessionStatusId() != null) {
                existing.setSessionStatusId(session.getSessionStatusId());
            }

            validator.validateSessionForSave(existing, id);
            return classSessionRepository.save(existing);
        }).orElseThrow(() -> new NotFoundException("Sesión no encontrada con id " + id));
    }

    @Override
    public void deleteSession(Long id) {
        classSessionRepository.deleteById(id);
    }

    @Override
    public List<ClassSession> getSessionsByCourse(Long courseId) {
        return classSessionRepository.findByCourseId(courseId);
    }

    @Override
    public List<ClassSession> getSessionsBySubject(Long subjectId) {
        return classSessionRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<ClassSession> getSessionsByTeacher(Long teacherId) {
        return classSessionRepository.findByTeacherId(teacherId);
    }
}
