package cl.duoc.libroDigital.attendanceService.service;

import cl.duoc.libroDigital.attendanceService.model.ClassSession;

import java.util.List;
import java.util.Optional;

public interface ClassSessionService {

    ClassSession createSession(ClassSession session);

    List<ClassSession> getAllSessions();

    Optional<ClassSession> getSessionById(Long id);

    ClassSession updateSession(Long id, ClassSession session);

    void deleteSession(Long id);

    List<ClassSession> getSessionsByCourse(Long courseId);

    List<ClassSession> getSessionsBySubject(Long subjectId);

    List<ClassSession> getSessionsByTeacher(Long teacherId);
}
