package cl.duoc.libroDigital.attendanceService.controller;

import cl.duoc.libroDigital.attendanceService.dto.AnnotationDTO;
import cl.duoc.libroDigital.attendanceService.exception.ForbiddenException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.security.AttendanceAccessService;
import cl.duoc.libroDigital.attendanceService.service.AnnotationService;
import cl.duoc.libroDigital.attendanceService.service.AttendanceCatalogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnotationControllerTest {

    @Mock
    private AnnotationService annotationService;
    @Mock
    private AttendanceAccessService access;
    @Mock
    private AttendanceCatalogService catalogs;

    @InjectMocks
    private AnnotationController annotationController;

    @Test
    void createAnnotation_teacherRoleOverridesTeacherId() {
        AnnotationDTO dto = new AnnotationDTO();
        dto.setStudentId(30L);
        dto.setTeacherId(999L);
        dto.setAnnotationDate(LocalDate.of(2026, 6, 10));
        dto.setType("POSITIVA");

        Annotation created = new Annotation();
        created.setId(15L);
        created.setStudentId(30L);
        created.setTeacherId(45L);
        created.setAnnotationTypeId((short) 1);

        when(catalogs.annotationTypeId("POSITIVA")).thenReturn((short) 1);
        when(access.isTeacher()).thenReturn(true);
        when(access.isAdmin()).thenReturn(false);
        when(access.requireTeacherId()).thenReturn(45L);
        when(annotationService.createAnnotation(any(Annotation.class))).thenReturn(created);
        when(access.resolveStudentName(30L)).thenReturn(Optional.of("Camila Soto"));
        when(catalogs.annotationTypeCode((short) 1)).thenReturn("POSITIVA");

        AnnotationDTO response = annotationController.createAnnotation(dto);

        assertEquals(15L, response.getId());
        assertEquals(45L, response.getTeacherId());
        assertEquals("Camila Soto", response.getStudentName());
    }

    @Test
    void getAnnotationsByTeacher_nonAdminCannotReadOtherTeacher() {
        when(access.isAdmin()).thenReturn(false);
        when(access.requireTeacherId()).thenReturn(8L);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> annotationController.getAnnotationsByTeacher(9L));

        assertEquals("No puede consultar anotaciones de otro docente", ex.getMessage());
    }

    @Test
    void getAllAnnotations_adminReturnsAllAnnotations() {
        Annotation annotation = new Annotation();
        annotation.setId(2L);
        annotation.setStudentId(60L);
        annotation.setAnnotationTypeId((short) 2);

        when(access.isAdmin()).thenReturn(true);
        when(annotationService.getAllAnnotations()).thenReturn(List.of(annotation));
        when(access.resolveStudentName(60L)).thenReturn(Optional.of("Alumno Dos"));
        when(catalogs.annotationTypeCode((short) 2)).thenReturn("NEGATIVA");

        List<AnnotationDTO> response = annotationController.getAllAnnotations();

        assertEquals(1, response.size());
        assertEquals(2L, response.getFirst().getId());
        verify(annotationService).getAllAnnotations();
    }
}
