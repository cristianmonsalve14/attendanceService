package cl.duoc.libroDigital.attendanceService.service;

import cl.duoc.libroDigital.attendanceService.exception.NotFoundException;
import cl.duoc.libroDigital.attendanceService.model.Annotation;
import cl.duoc.libroDigital.attendanceService.repository.AnnotationRepository;
import cl.duoc.libroDigital.attendanceService.service.impl.AnnotationServiceImpl;
import cl.duoc.libroDigital.attendanceService.validation.AttendanceEntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnotationServiceImplTest {

    @Mock
    private AnnotationRepository annotationRepository;

    @Mock
    private AttendanceEntityValidator validator;

    @InjectMocks
    private AnnotationServiceImpl annotationService;

    @Test
    void createAnnotation_validatesAndSavesAnnotation() {
        Annotation annotation = validAnnotation();
        when(annotationRepository.save(annotation)).thenReturn(annotation);

        Annotation saved = annotationService.createAnnotation(annotation);

        assertSame(annotation, saved);
        verify(validator).validateAnnotationForSave(annotation);
        verify(annotationRepository).save(annotation);
    }

    @Test
    void getAllAnnotations_returnsRepositoryData() {
        when(annotationRepository.findAll()).thenReturn(List.of(validAnnotation()));

        List<Annotation> annotations = annotationService.getAllAnnotations();

        assertEquals(1, annotations.size());
        verify(annotationRepository).findAll();
    }

    @Test
    void getAnnotationById_returnsRepositoryResult() {
        Annotation annotation = validAnnotation();
        when(annotationRepository.findById(1L)).thenReturn(Optional.of(annotation));

        Optional<Annotation> response = annotationService.getAnnotationById(1L);

        assertTrue(response.isPresent());
        assertSame(annotation, response.get());
        verify(annotationRepository).findById(1L);
    }

    @Test
    void updateAnnotation_updatesOnlyProvidedFieldsAndSaves() {
        Annotation existing = validAnnotation();
        existing.setId(1L);
        existing.setDescription("Descripción inicial");

        Annotation update = new Annotation();
        update.setDescription("Nueva descripción");
        update.setAnnotationTypeId((short) 2);

        when(annotationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(annotationRepository.save(existing)).thenReturn(existing);

        Annotation updated = annotationService.updateAnnotation(1L, update);

        assertEquals("Nueva descripción", updated.getDescription());
        assertEquals((short) 2, updated.getAnnotationTypeId());
        assertEquals(100L, updated.getStudentId());
        verify(validator).validateAnnotationForSave(existing);
        verify(annotationRepository).save(existing);
    }

    @Test
    void updateAnnotation_throwsWhenAnnotationDoesNotExist() {
        when(annotationRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> annotationService.updateAnnotation(99L, new Annotation()));

        assertTrue(ex.getMessage().contains("99"));
        verify(annotationRepository, never()).save(any(Annotation.class));
    }

    @Test
    void deleteAnnotation_delegatesToRepository() {
        annotationService.deleteAnnotation(9L);

        verify(annotationRepository).deleteById(9L);
    }

    @Test
    void getAnnotationsByStudent_returnsRepositoryData() {
        when(annotationRepository.findByStudentId(100L)).thenReturn(List.of(validAnnotation()));

        List<Annotation> annotations = annotationService.getAnnotationsByStudent(100L);

        assertEquals(1, annotations.size());
        verify(annotationRepository).findByStudentId(100L);
    }

    @Test
    void getAnnotationsByTeacher_returnsRepositoryData() {
        when(annotationRepository.findByTeacherId(200L)).thenReturn(List.of(validAnnotation()));

        List<Annotation> annotations = annotationService.getAnnotationsByTeacher(200L);

        assertEquals(1, annotations.size());
        verify(annotationRepository).findByTeacherId(200L);
    }

    private static Annotation validAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setStudentId(100L);
        annotation.setTeacherId(200L);
        annotation.setAnnotationDate(LocalDate.now().minusDays(1));
        annotation.setAnnotationTypeId((short) 1);
        annotation.setDescription("Anotación válida");
        return annotation;
    }
}
