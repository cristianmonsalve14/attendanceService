package cl.duoc.libroDigital.attendanceService.repository;

import cl.duoc.libroDigital.attendanceService.model.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    List<Annotation> findByStudentId(Long studentId);

    List<Annotation> findByTeacherId(Long teacherId);
}
