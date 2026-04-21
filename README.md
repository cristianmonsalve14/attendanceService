# attendanceService

Microservicio de asistencia y anotaciones de la plataforma Libro Digital.

Gestiona el registro de asistencia por sesión de clase y las anotaciones de conducta (positivas y negativas) de los estudiantes. Desarrollado con Spring Boot 4.0.5, Java 21 y Maven. Forma parte de la arquitectura de microservicios para la gestión académica.

## Stack tecnológico
- Java 21
- Spring Boot 4.0.5
- Spring Web
- Spring Data JPA
- Spring Security
- Maven
- PostgreSQL

## Instalación y ejecución
1. Clona este repositorio.
2. Configura la conexión a la base de datos en `src/main/resources/application.properties`.
3. Compila y ejecuta con:
   ```sh
   mvn clean spring-boot:run
   ```

## Autores
- Cristian Monsalve
- Hector Olivares

---
Este microservicio es parte del ecosistema Libro Digital. Más información y documentación general en el repositorio de infraestructura.