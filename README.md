# HomeLink - Backend (Spring Boot)

Version inicial (MVP) de la plataforma HomeLink: conecta clientes con trabajadores
independientes de servicios para el hogar (plomeria, electricidad, pintura,
cerrajeria) en Cartagena, Colombia.

## Stack

- Java 21 + Spring Boot 3.3.4
- Thymeleaf (renderizado en servidor)
- Spring Data JPA + MySQL
- BCrypt (spring-security-crypto) para hash de contrasenas

## Como ejecutar

1. Crea una base de datos MySQL local (o deja que se autocree gracias a
   `createDatabaseIfNotExist=true` en `application.properties`).
2. Ajusta `src/main/resources/application.properties` con tu usuario y
   contrasena de MySQL.
3. Ejecuta:

   ```bash
   mvn spring-boot:run
   ```

4. La app queda disponible en `http://localhost:8080`.

## Datos de prueba

Al arrancar por primera vez se crean 4 categorias y un usuario administrador:

- Email: `admin@homelink.com`
- Password: `admin123`

## Estado del proyecto

Este es un primer corte funcional (MVP), construido rapido para tener una base
real sobre la cual hacer el diagnostico tecnico del Taller 1 (arquitectura,
configuracion, manejo de errores, seguridad, documentacion de API y pruebas).
A propósito, todavia NO incluye: Spring Security completo, perfiles
dev/prod, `@ControllerAdvice` global, Swagger/OpenAPI, ni pruebas mas alla del
arranque del contexto - esos son precisamente los puntos a evaluar y luego
mejorar en los siguientes laboratorios.
