package cl.duoc.libroDigital.attendanceService.service.impl;

import cl.duoc.libroDigital.attendanceService.exception.NotFoundException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.repository.AnnotationRepository;
import cl.duoc.libroDigital.attendanceService.service.AnnotationService;
import cl.duoc.libroDigital.attendanceService.validation.AttendanceEntityValidator;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnotationServiceImpl implements AnnotationService {

    private final AnnotationRepository annotationRepository;
    private final AttendanceEntityValidator validator;

    public AnnotationServiceImpl(AnnotationRepository annotationRepository, AttendanceEntityValidator validator) {
        this.annotationRepository = annotationRepository;
        this.validator = validator;
    }

    @Override
    public Annotation createAnnotation(Annotation annotation) {
        validator.validateAnnotationForSave(annotation);
        return annotationRepository.save(annotation);
    }

    @Override
    public List<Annotation> getAllAnnotations() {
        return annotationRepository.findAll();
    }

    @Override
    public Optional<Annotation> getAnnotationById(Long id) {
        return annotationRepository.findById(id);
    }

    @Override
    public Annotation updateAnnotation(Long id, Annotation annotation) {
        return annotationRepository.findById(id).map(existing -> {
            if (annotation.getStudentId() != null) existing.setStudentId(annotation.getStudentId());
            if (annotation.getTeacherId() != null) existing.setTeacherId(annotation.getTeacherId());
            if (annotation.getAnnotationDate() != null) existing.setAnnotationDate(annotation.getAnnotationDate());
            if (annotation.getAnnotationTypeId() != null) existing.setAnnotationTypeId(annotation.getAnnotationTypeId());
            if (annotation.getDescription() != null) existing.setDescription(annotation.getDescription());

            validator.validateAnnotationForSave(existing);
            return annotationRepository.save(existing);
        }).orElseThrow(() -> new NotFoundException("Anotación no encontrada con id " + id));
    }

    @Override
    public void deleteAnnotation(Long id) {
        annotationRepository.deleteById(id);
    }

    @Override
    public List<Annotation> getAnnotationsByStudent(Long studentId) {
        return annotationRepository.findByStudentId(studentId);
    }

    @Override
    public List<Annotation> getAnnotationsByTeacher(Long teacherId) {
        return annotationRepository.findByTeacherId(teacherId);
    }
}
