package cl.duoc.libroDigital.attendanceService.controller;

import cl.duoc.libroDigital.attendanceService.dto.AnnotationDTO;
import cl.duoc.libroDigital.attendanceService.exception.ForbiddenException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.security.AttendanceAccessService;
import cl.duoc.libroDigital.attendanceService.service.AnnotationService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/annotations")
public class AnnotationController {

    private final AnnotationService annotationService;
    private final AttendanceAccessService access;
    private final AttendanceCatalogService catalogs;

    public AnnotationController(
            AnnotationService annotationService,
            AttendanceAccessService access,
            AttendanceCatalogService catalogs) {
        this.annotationService = annotationService;
        this.access = access;
        this.catalogs = catalogs;
    }

    private AnnotationDTO toDTO(Annotation annotation) {
        AnnotationDTO dto = new AnnotationDTO();
        dto.setId(annotation.getId());
        dto.setStudentId(annotation.getStudentId());
        access.resolveStudentName(annotation.getStudentId()).ifPresent(dto::setStudentName);
        dto.setTeacherId(annotation.getTeacherId());
        dto.setAnnotationDate(annotation.getAnnotationDate());
        dto.setType(catalogs.annotationTypeCode(annotation.getAnnotationTypeId()));
        dto.setDescription(annotation.getDescription());
        dto.setCreatedAt(annotation.getCreatedAt());
        dto.setUpdatedAt(annotation.getUpdatedAt());
        return dto;
    }

    private Annotation toEntity(AnnotationDTO dto) {
        Annotation annotation = new Annotation();
        annotation.setId(dto.getId());
        annotation.setStudentId(dto.getStudentId());
        annotation.setTeacherId(dto.getTeacherId());
        annotation.setAnnotationDate(dto.getAnnotationDate());
        annotation.setAnnotationTypeId(catalogs.annotationTypeId(dto.getType()));
        annotation.setDescription(dto.getDescription());
        return annotation;
    }

    @PostMapping
    public AnnotationDTO createAnnotation(@RequestBody AnnotationDTO dto) {
        Annotation entity = toEntity(dto);
        if (access.isTeacher() && !access.isAdmin()) {
            entity.setTeacherId(access.requireTeacherId());
        }
        return toDTO(annotationService.createAnnotation(entity));
    }

    @GetMapping
    public List<AnnotationDTO> getAllAnnotations() {
        if (access.isAdmin()) {
            return annotationService.getAllAnnotations()
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        Long teacherId = access.requireTeacherId();
        return annotationService.getAnnotationsByTeacher(teacherId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AnnotationDTO getAnnotation(@PathVariable Long id) {
        access.ensureCanManageAnnotation(id);
        return annotationService.getAnnotationById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @GetMapping("/student/{studentId}")
    public List<AnnotationDTO> getAnnotationsByStudent(@PathVariable Long studentId) {
        access.ensureCanReadStudent(studentId);
        return annotationService.getAnnotationsByStudent(studentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/teacher/{teacherId}")
    public List<AnnotationDTO> getAnnotationsByTeacher(@PathVariable Long teacherId) {
        if (!access.isAdmin()) {
            Long currentTeacherId = access.requireTeacherId();
            if (!currentTeacherId.equals(teacherId)) {
                throw new ForbiddenException("No puede consultar anotaciones de otro docente");
            }
        }
        return annotationService.getAnnotationsByTeacher(teacherId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public AnnotationDTO updateAnnotation(@PathVariable Long id, @RequestBody AnnotationDTO dto) {
        access.ensureCanManageAnnotation(id);
        Annotation entity = toEntity(dto);
        if (access.isTeacher() && !access.isAdmin()) {
            entity.setTeacherId(access.requireTeacherId());
        }
        return toDTO(annotationService.updateAnnotation(id, entity));
    }

    @DeleteMapping("/{id}")
    public void deleteAnnotation(@PathVariable Long id) {
        access.ensureCanManageAnnotation(id);
        annotationService.deleteAnnotation(id);
    }
}
