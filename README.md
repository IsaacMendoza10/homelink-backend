# HomeLink - Backend (Spring Boot)

Version inicial (MVP) de la plataforma HomeLink: conecta clientes con trabajadores
independientes de servicios para el hogar (plomeria, electricidad, pintura,
cerrajeria) en Cartagena, Colombia.

## Stack

- Java 21 + Spring Boot 3.3.4
- Thymeleaf (renderizado en servidor)
- Spring Data JPA + MySQL
- Spring Security (autenticacion por formulario + autorizacion por rol) con BCrypt para el hash de contrasenas

## Como ejecutar

1. Crea una base de datos MySQL local (o deja que se autocree gracias a
   `createDatabaseIfNotExist=true` en `application.properties`).
2. Si tu usuario/contrasena de MySQL no son `root`/`root`, define las variables
   de entorno `DB_USERNAME` y `DB_PASSWORD` antes de arrancar (si no las defines,
   se usa `root`/`root` por defecto en desarrollo local).
3. Ejecuta:

   ```bash
   mvn spring-boot:run
   ```

4. La app queda disponible en `http://localhost:8080`.

## Datos de prueba

Al arrancar por primera vez se crean 4 categorias y un usuario administrador:

- Email: `admin@homelink.com`
- Password: `admin123`

## Seguridad

El acceso esta protegido con Spring Security (ver `config/SecurityConfig.java`):

- Publico, sin iniciar sesion: home, login, registro, listado de categorias y
  de trabajadores (GET), y los endpoints `/api/estado` y `/api/trabajadores/buscar`.
- Requiere haber iniciado sesion (cualquier rol): `/perfil/**` y `/solicitudes/**`.
- Requiere el rol `ADMINISTRADOR`: `/admin/**`, crear/eliminar categorias
  (`POST /categorias/nueva`, `POST /categorias/{id}/eliminar`) y `/actuator/**`
  (salvo `/actuator/health`, que es publico).

El login vive en `GET/POST /login` (Spring Security intercepta el POST) y el
logout en `GET /logout`.

## Estado del proyecto

Este proyecto arranco como un MVP rapido, construido a proposito sin Spring
Security completo, para servir de base real al diagnostico tecnico del
Taller 1 (arquitectura, configuracion, manejo de errores, seguridad,
documentacion de API y pruebas). Desde entonces se le sumaron:

- Un pipeline de CI/CD con GitHub Actions (build, pruebas con JUnit, cobertura
  con JaCoCo, rama `main` protegida por Pull Request).
- Autenticacion y autorizacion por rol con Spring Security (este cambio).

Pendiente para siguientes iteraciones: perfiles de configuracion dev/prod,
`@ControllerAdvice` global para manejo de errores, y Swagger/OpenAPI.
