# attendanceService

Microservicio de asistencia y anotaciones de la plataforma **Libro Digital**.

Gestiona sesiones de clase, registro de asistencia por estudiante y anotaciones de conducta (positivas y negativas).

---

## Stack tecnológico

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Maven

---

## Puerto

http://localhost:8093

Vía API Gateway: http://localhost:8090

---

## Base de datos

`librodigital_attendance` (se configurará en la fase de bases de datos).

Tablas (JPA `ddl-auto=update`):

- `class_sessions` — sesiones de clase
- `attendance_records` — asistencia por estudiante y sesión
- `annotations` — anotaciones de conducta

Referencias a `courseId`, `subjectId`, `teacherId`, `studentId` son IDs lógicos del `academicService` (sin FK entre bases).

---

## Endpoints

Todas las rutas requieren JWT:

`Authorization: Bearer {token}`

### Sesiones (`/sessions`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/sessions` | Crear sesión |
| GET | `/sessions` | Listar sesiones |
| GET | `/sessions/{id}` | Obtener por ID |
| GET | `/sessions/course/{courseId}` | Por curso |
| GET | `/sessions/subject/{subjectId}` | Por asignatura |
| PUT | `/sessions/{id}` | Actualizar |
| DELETE | `/sessions/{id}` | Eliminar |

### Asistencias (`/attendances`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/attendances` | Registrar asistencia |
| GET | `/attendances` | Listar todas |
| GET | `/attendances/{id}` | Obtener por ID |
| GET | `/attendances/session/{sessionId}` | Por sesión |
| GET | `/attendances/student/{studentId}` | Por estudiante |
| PUT | `/attendances/{id}` | Actualizar |
| DELETE | `/attendances/{id}` | Eliminar |

**Estados válidos:** `PRESENTE`, `AUSENTE`, `ATRASADO`, `JUSTIFICADO`

### Anotaciones (`/annotations`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/annotations` | Crear anotación |
| GET | `/annotations` | Listar todas |
| GET | `/annotations/{id}` | Obtener por ID |
| GET | `/annotations/student/{studentId}` | Por estudiante |
| GET | `/annotations/teacher/{teacherId}` | Por docente |
| PUT | `/annotations/{id}` | Actualizar |
| DELETE | `/annotations/{id}` | Eliminar |

**Tipos válidos:** `POSITIVA`, `NEGATIVA`

---

## Instalación y ejecución

```sh
mvn clean spring-boot:run
```

---

## Autores

- Cristian Monsalve
- Hector Olivares

---

Este microservicio es parte del ecosistema Libro Digital. Más información en el repositorio de infraestructura.
