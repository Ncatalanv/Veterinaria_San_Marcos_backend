# Veterinaria San Marcos - Backend

API REST para la gestión de la Veterinaria San Marcos. El proyecto está construido con Spring Boot, MySQL y Flyway, y será consumido por el frontend React del repositorio `Veterinaria_San_Marcos.github.io`.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- MySQL 8.0
- Flyway
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper
- Lombok

## Requisitos

- JDK 21 instalado y configurado en `JAVA_HOME`.
- MySQL 8.0 ejecutándose en `localhost:3306`.
- Git.
- Opcional: MySQL Workbench o DBeaver para revisar la base de datos.

## Base de datos

El proyecto usa una base llamada `sanmarcos_db` y un usuario dedicado llamado `sanmarcos_app`. La aplicación no debe conectarse con el usuario `root`.

Crea la base y el usuario desde MySQL:

```sql
CREATE DATABASE IF NOT EXISTS sanmarcos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'sanmarcos_app'@'localhost'
  IDENTIFIED BY 'CAMBIAR_POR_UNA_CLAVE_SEGURA';

GRANT ALL PRIVILEGES ON sanmarcos_db.* TO 'sanmarcos_app'@'localhost';
FLUSH PRIVILEGES;
```

Si el usuario ya existe y necesitas cambiar su contraseña:

```sql
ALTER USER 'sanmarcos_app'@'localhost'
  IDENTIFIED BY 'CAMBIAR_POR_UNA_CLAVE_SEGURA';
FLUSH PRIVILEGES;
```

## Variables de configuración

La configuración base está en `src/main/resources/application.yml` y usa variables de entorno con valores por defecto para desarrollo local.

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/sanmarcos_db?serverTimezone=America/Santiago&useSSL=false&allowPublicKeyRetrieval=true` | URL JDBC de MySQL. |
| `DB_USERNAME` | `sanmarcos_app` | Usuario de MySQL. |
| `DB_PASSWORD` | vacío | Contraseña del usuario de MySQL. En local se define en `application-local.yml`. |
| `SERVER_PORT` | `8080` | Puerto HTTP del backend. |

Variables que se incorporarán en las siguientes fases:

| Variable | Valor previsto | Descripción |
|---|---|---|
| `JWT_SECRET` | sin valor por defecto | Secreto para firmar JWT. Debe tener al menos 32 caracteres. |
| `JWT_EXPIRATION` | `7200` | Vigencia del token en segundos. |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:4173,https://ncatalanv.github.io` | Orígenes permitidos por CORS. |

En producción, configura estas variables en el entorno de la instancia EC2. No guardes secretos reales en archivos versionados.

## Configuración local

1. Copia la plantilla:

```powershell
Copy-Item src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

2. Edita `src/main/resources/application-local.yml` con tu contraseña local:

```yaml
spring:
  datasource:
    username: sanmarcos_app
    password: "TU_CLAVE_LOCAL"
  jpa:
    show-sql: true
```

3. Verifica que el archivo esté ignorado por Git:

```powershell
git check-ignore -v src/main/resources/application-local.yml
```

`application-local.yml` contiene credenciales locales y **no debe commitearse**. El archivo versionado es únicamente `application-local.yml.example`.

## Ejecutar el backend

En Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

> En PowerShell, los argumentos `-D` que contienen puntos deben ir entre comillas. Sin comillas, PowerShell puede dividir el argumento y Maven mostrará `Unknown lifecycle phase`.

En Linux o macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

La aplicación queda disponible en `http://localhost:8080`.

Para compilar sin ejecutar pruebas:

```powershell
.\mvnw.cmd -q -DskipTests package
```

## Swagger y OpenAPI

Con la aplicación levantada:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Documento OpenAPI: `http://localhost:8080/v3/api-docs`
- Health check: se agregará en la tarjeta de CORS/Swagger como `GET /api/v1/health`.

El esquema de seguridad Bearer JWT se habilitará en la Fase 1.

## Flyway y esquema

Flyway es la fuente de verdad del esquema. Las migraciones están en:

```text
src/main/resources/db/migration/
```

La migración `V1__schema.sql` crea estas tablas:

- `roles`
- `usuarios`
- `mascotas`
- `categorias`
- `productos`
- `servicios`
- `ordenes`
- `orden_detalle`
- `solicitudes_servicio`
- `posts`

También inserta los roles iniciales:

- `ADMIN`
- `VENDEDOR`
- `VETERINARIO`
- `CLIENTE`

Regla importante: una migración ya aplicada no se edita. Cualquier cambio posterior debe ir en una nueva migración, por ejemplo `V2__...sql`.

Verificación rápida en MySQL:

```sql
USE sanmarcos_db;
SHOW TABLES;
SELECT * FROM roles ORDER BY id;
SELECT version, description, success FROM flyway_schema_history;
```

Resultado esperado: 10 tablas del proyecto, la tabla `flyway_schema_history`, cuatro roles y la versión `1` aplicada correctamente.

## Estructura del proyecto

```text
src/main/java/com/frontendlovers/sanmarcos/
  SanmarcosApplication.java

src/main/resources/
  application.yml
  application-local.yml
  application-local.yml.example
  db/migration/V1__schema.sql
```

## Pruebas

Los tests automatizados se configurarán en la Fase 1 con un perfil de prueba independiente. Por ahora, el test de contexto usa la configuración por defecto y puede fallar si no existe una contraseña de base de datos disponible en el entorno.

Para una verificación de Fase 0, usa el arranque con el perfil `local` y revisa los logs de Flyway y Swagger.

## Solución de problemas

- **`Access denied for user 'sanmarcos_app'@'localhost'`**: revisa la contraseña en `application-local.yml` y los permisos del usuario en MySQL.
- **`Unknown database 'sanmarcos_db'`**: crea la base con el script de este README.
- **Puerto 8080 ocupado**: detén el proceso que lo usa o ejecuta con `SERVER_PORT=8081`.
- **Flyway checksum mismatch**: no edites una migración ya aplicada. Crea una migración nueva con `V2`, `V3`, etc.
- **Swagger no carga**: confirma que la aplicación esté levantada y que la dependencia `springdoc-openapi-starter-webmvc-ui` esté en `pom.xml`.
