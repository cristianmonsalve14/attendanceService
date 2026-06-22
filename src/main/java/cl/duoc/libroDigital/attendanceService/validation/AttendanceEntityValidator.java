package cl.duoc.libroDigital.attendanceService.validation;

import cl.duoc.libroDigital.attendanceService.exception.BadRequestException;
import cl.duoc.libroDigital.attendanceService.exception.ConflictException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.model.ClassSession;
import cl.duoc.libroDigital.attendanceService.repository.ClassSessionRepository;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class AttendanceEntityValidator {

    private static final Set<Short> VALID_ANNOTATION_TYPES = Set.of((short) 1, (short) 2);

    private final ClassSessionRepository classSessionRepository;

    public AttendanceEntityValidator(ClassSessionRepository classSessionRepository) {
        this.classSessionRepository = classSessionRepository;
    }

    public void validateSessionForSave(ClassSession session, Long excludeId) {
        if (session.getSubjectId() == null) {
            throw new BadRequestException("La asignatura es obligatoria");
        }
        if (session.getCourseId() == null) {
            throw new BadRequestException("El curso es obligatorio");
        }
        if (session.getTeacherId() == null || session.getTeacherId() <= 0) {
            throw new BadRequestException("El docente es obligatorio");
        }
        if (session.getSessionDate() == null) {
            throw new BadRequestException("La fecha de la sesión es obligatoria");
        }
        if (session.getSessionDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de la sesión no puede ser futura");
        }

        classSessionRepository.findBySubjectIdAndSessionDate(session.getSubjectId(), session.getSessionDate())
                .stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .findAny()
                .ifPresent(existing -> {
                    throw new ConflictException("Ya existe una sesión de esta asignatura en esa fecha");
                });
    }

    public void validateAnnotationForSave(Annotation annotation) {
        if (annotation.getStudentId() == null) {
            throw new BadRequestException("El estudiante es obligatorio");
        }
        if (annotation.getTeacherId() == null || annotation.getTeacherId() <= 0) {
            throw new BadRequestException("El docente es obligatorio");
        }
        if (annotation.getAnnotationDate() == null) {
            throw new BadRequestException("La fecha de la anotación es obligatoria");
        }
        if (annotation.getAnnotationDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de la anotación no puede ser futura");
        }
        if (annotation.getAnnotationTypeId() == null || !VALID_ANNOTATION_TYPES.contains(annotation.getAnnotationTypeId())) {
            throw new BadRequestException("El tipo de anotación es inválido. Valores permitidos: POSITIVA, NEGATIVA");
        }
        if (annotation.getDescription() == null || annotation.getDescription().isBlank()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
        annotation.setDescription(annotation.getDescription().trim());
    }
}
