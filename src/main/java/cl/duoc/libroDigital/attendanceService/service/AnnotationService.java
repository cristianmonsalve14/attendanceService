package cl.duoc.libroDigital.attendanceService.service;

import cl.duoc.libroDigital.attendanceService.model.Annotation;

import java.util.List;
import java.util.Optional;

public interface AnnotationService {

    Annotation createAnnotation(Annotation annotation);

    List<Annotation> getAllAnnotations();

    Optional<Annotation> getAnnotationById(Long id);

    Annotation updateAnnotation(Long id, Annotation annotation);

    void deleteAnnotation(Long id);

    List<Annotation> getAnnotationsByStudent(Long studentId);

    List<Annotation> getAnnotationsByTeacher(Long teacherId);
}
