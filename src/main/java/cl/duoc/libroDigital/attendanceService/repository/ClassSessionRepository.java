package cl.duoc.libroDigital.attendanceService.repository;

import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByCourseId(Long courseId);

    List<ClassSession> findBySubjectId(Long subjectId);

    List<ClassSession> findByTeacherId(Long teacherId);

    List<ClassSession> findBySessionDate(LocalDate sessionDate);

    List<ClassSession> findBySubjectIdAndSessionDate(Long subjectId, LocalDate sessionDate);
}
