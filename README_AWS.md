# Veterinaria San Marcos — Backend en AWS

Guía operativa para **volver a encender** el backend, **consultar su IP** y **qué hacer cuando la IP cambia**.

> ⚠️ La instancia usa una **IP pública automática**, no una Elastic IP. Al **detener y volver a iniciar** la instancia (*Stop → Start*), la IP cambia y el frontend debe recompilarse con la nueva URL. Un **Restart** (reinicio) conserva la IP.

## 1. Datos del despliegue

| Concepto | Valor |
|---|---|
| Región | `us-east-1` (Zona de disponibilidad `us-east-1b`) |
| Instancia | `i-0ce5ae680699b5b2a` |
| Tipo | `t3.micro`, Ubuntu 24.04 LTS |
| Usuario de conexión | `ubuntu` (el servicio corre como `root` para enlazar el puerto 80) |
| Key pair | `vet_sm_backend` |
| IP pública | `52.7.237.230` — **verificar siempre** (ver §3) |
| JAR | `/home/ubuntu/sanmarcos-0.0.1-SNAPSHOT.jar` |
| Variables de entorno | `/etc/sanmarcos.env` |
| Servicio systemd | `sanmarcos` |
| Base de datos | MySQL 8, base `sanmarcos_db`, usuario `sanmarcos_app` |
| Swagger | `http://<IP>/swagger-ui/index.html` |
| Ping | `http://<IP>/api/v1/ping` → `pong` |
| Health | `http://<IP>/actuator/health` → `{"status":"UP"}` |
| Logs en AWS | CloudWatch: `/aws/sanmarcos/backend` y `/aws/sanmarcos/heartbeat` |
| Repositorios | Backend `Ncatalanv/Veterinaria_San_Marcos_backend`, frontend `jocfapaz/Veterinaria_San_Marcos_Frontend` |
| Bucket del frontend | `veterinaria-san-marcos-frontend` (`us-east-1`) |
| URL del frontend | `http://veterinaria-san-marcos-frontend.s3-website-us-east-1.amazonaws.com` |
| API usada por el frontend | `VITE_API_URL=http://<IP>/api/v1` (sin `:8080`) |

> No guardes contraseñas reales en este archivo. Las credenciales viven en `/etc/sanmarcos.env`, dentro de la instancia, con permisos `600`.

## 2. Encender y apagar la instancia

Consola AWS → **EC2 → Instances** → seleccionar `i-0ce5ae680699b5b2a` → **Instance state → Start / Stop / Restart**.

- Esperar a que diga **Running** y *Status checks* en `2/2 passed`.
- **Restart conserva la IP**. **Stop + Start la cambia**.

Después de encender, dentro de la instancia:

```bash
sudo systemctl status sanmarcos
curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost/v3/api-docs
```

Esperado: `active (running)` y `HTTP 200`.

## 3. Cómo ver la IP pública

**Desde la consola:** EC2 → Instances → columna *Public IPv4 address*.

**Desde AWS CloudShell:**

```bash
aws --region us-east-1 ec2 describe-instances \
  --instance-ids i-0ce5ae680699b5b2a \
  --query "Reservations[].Instances[].PublicIpAddress" --output text
```

**Desde la propia instancia:**

```bash
curl -s http://169.254.169.254/latest/meta-data/public-ipv4
echo
```

Si no devuelve nada (la instancia exige IMDSv2):

```bash
TOKEN=$(curl -sX PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/public-ipv4
echo
```

**Desde tu PC (comprobación final):**

```powershell
curl.exe http://<IP>/v3/api-docs
```

## 4. Si la IP cambió

El backend sigue funcionando, pero el frontend tiene la URL anterior **incrustada en el build**. Pasos:

1. Actualizar `.env.production` en el frontend:

   ```env
   VITE_API_URL=http://<NUEVA_IP>/api/v1
   ```

2. Recompilar y volver a subir:

   ```powershell
   cd "C:\Users\nico_\Desktop\Classes\Fullstack II\Veterinaria San Marcos\Veterinaria_San_Marcos.github.io"
   npm run build
   aws s3 sync .\dist s3://veterinaria-san-marcos-frontend/ --delete --exclude "*.jar"
   ```

   O subir el **contenido** de `dist/` desde la consola S3 (sin la carpeta `dist` en sí).

3. Verificar desde el PC: `curl.exe http://<NUEVA_IP>/v3/api-docs`.
4. Abrir el frontend y revisar con **F12 → Network** que no haya errores de CORS.

No hay que tocar el Security Group: sus reglas (SSH 22 y TCP 80) están ligadas a la instancia, no a la IP. Tampoco hay que cambiar `CORS_ORIGINS`: depende del bucket S3, no de la IP de la EC2.

### Opcional: IP fija

Para no repetir este proceso, reserva una **Elastic IP** y asígnala a la instancia:
*EC2 → Elastic IPs → Allocate Elastic IP address → Actions → Associate Elastic IP address*.

Luego recompila el frontend con esa IP. Una EIP es gratis mientras esté asociada a una instancia en ejecución; una EIP sin asociar puede generar cargo.

## 5. Si el backend no responde

Dentro de la instancia:

```bash
sudo systemctl status sanmarcos
sudo journalctl -u sanmarcos -n 100 --no-pager
sudo systemctl restart sanmarcos
sudo ss -tlnp | grep ':80'
sudo systemctl status mysql
sudo grep CORS /etc/sanmarcos.env
```

`grep` necesita `sudo` porque `/etc/sanmarcos.env` tiene permisos `600`.

### Si el log dice `Migration checksum mismatch`

Síntoma típico:

```
FlywayValidateException: Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 2 / 3
```

Causa: se editaron `V2__seed_catalog.sql` y `V3__seed_posts.sql` **después** de que Flyway ya las había aplicado en la BD. Flyway no permite cambiar el contenido de una migración aplicada. En este repo ya está corregido: V2 y V3 volvieron a su contenido original y el cambio de rutas de imágenes se movió a la migración nueva `V5__fix_seed_image_paths.sql`.

Si una BD ya registró otros checksums (por ejemplo, se desplegó una versión intermedia), hay que alinear el historial con el contenido del JAR **una sola vez**. En el log aparecen los valores esperados:

```
Migration checksum mismatch for migration version 2
-> Applied to database : -1134182992
-> Resolved locally    : 607015317
Migration checksum mismatch for migration version 3
-> Applied to database : 1499441380
-> Resolved locally    : 1590691046
```

Actualizar los checksums con los valores `Resolved locally` y reiniciar:

```bash
sudo mysql -e "UPDATE sanmarcos_db.flyway_schema_history SET checksum=607015317 WHERE version='2'; UPDATE sanmarcos_db.flyway_schema_history SET checksum=1590691046 WHERE version='3';"
sudo systemctl restart sanmarcos
curl -fsS http://127.0.0.1/actuator/health   # esperar a que responda UP
sudo mysql -N -e "SELECT installed_rank, version, description, success FROM sanmarcos_db.flyway_schema_history ORDER BY installed_rank;"
```

La última consulta debe mostrar también la fila `5 - fix seed image paths` con `success = 1`.

> ⚠️ No usar `SPRING_FLYWAY_REPAIR_ON_MIGRATE=true`: esa propiedad **no existe** en Spring Boot 3.3.5 (`FlywayProperties` no expone `repairOnMigrate`), por lo que el flag se ignora silenciosamente. El procedimiento de arriba es el equivalente a `flyway repair` para este caso.

> Regla para adelante: los cambios de datos van en una migración nueva (`V6`, `V7`, ...), nunca editando una ya aplicada.

Comprobaciones externas:

- Regla **inbound** `Custom TCP`, puerto **80**, source `0.0.0.0/0` en el Security Group de la instancia.
- Network ACL del subnet con `100 / All traffic / Allow / 0.0.0.0/0`.
- Estar usando la IP correcta (§3).

## 6. Entrar a la instancia

**Forma fácil:** EC2 → Instances → seleccionar → **Connect → EC2 Instance Connect**, usuario `ubuntu`.

**SSH desde PowerShell:**

```powershell
$key = "$env:USERPROFILE\Desktop\Classes\Fullstack II\Veterinaria San Marcos\aws\vet_sm_backend.pem"
icacls $key /inheritance:r
icacls $key /grant:r "$($env:USERNAME):(F)"
ssh -i $key ubuntu@<IP>
```

Si da `Permission denied (publickey)`, confirmar que la IP es la correcta (§3) y que la clave es `vet_sm_backend`.

## 7. Actualizar el backend con un nuevo JAR

1. En tu PC:

   ```powershell
   .\mvnw.cmd -q -DskipTests package
   ```

2. Subir el JAR al bucket S3 y descargarlo en la EC2 (método que funcionó, evita depender de SSH):

   ```bash
   curl -fL -o /home/ubuntu/sanmarcos-0.0.1-SNAPSHOT.jar \
     "https://veterinaria-san-marcos-frontend.s3.amazonaws.com/sanmarcos-0.0.1-SNAPSHOT.jar"
   sudo systemctl restart sanmarcos
   ```

3. Verificar:

   ```bash
   curl -s -o /dev/null -w "HTTP %{http_code}\n" http://localhost/v3/api-docs
   ```

## 8. Costos

La cuenta tiene free tier por 12 meses: EC2 `t3.micro` 750 h/mes, S3 5 GB y 100 GB de transferencia saliente. Apagar la instancia fuera de clase ahorra horas, pero **cambia la IP** (ver §4).

## 9. CI/CD con GitHub Actions

El despliegue ya no depende de ejecutar comandos a mano: un `push` a `main` dispara los workflows.

| Repositorio | Workflow | Qué hace |
|---|---|---|
| Backend | `.github/workflows/deploy-aws.yml` | `bash ./mvnw test` → `bash ./mvnw -DskipTests package` → sube el JAR a S3 → SSH a la EC2 para reiniciar `sanmarcos` y validar `/actuator/health`. |
| Frontend | `.github/workflows/deploy.yml` | `npm ci` → lint → tests → cobertura → `npm run build` con `VITE_API_URL` → publica en GitHub Pages **y** sincroniza `dist/` al bucket S3 (`--exclude "*.jar"` para no borrar el JAR del backend). |

- Los `pull_request` ejecutan solo CI; **no despliegan**. El deploy ocurre únicamente en `main`.
- El job de CI levanta un servicio `mysql:8.0` con la base `sanmarcos_db` vacía, así Flyway siembra los datos
  y los tests son reproducibles sin depender de la MySQL local.
- El JAR se sube a S3 y la EC2 lo descarga por su URL pública; el objeto debe ser legible públicamente.
- El reinicio de la EC2 se hace por SSH, no por Systems Manager: AWS Academy bloquea IAM/SSM (`iam:ListInstanceProfiles`, creación de usuarios/roles, etc.).
- Si la instancia cambia de IP, actualizar el secret `EC2_HOST`; si cambia la llave, actualizar `EC2_SSH_KEY`.
- Si falla cualquier etapa (`test`, `lint`, `build`), el despliegue no se ejecuta.
- Evidencia: pestaña **Actions** del repo → corrida → artefactos `jar` y `dist`.

### Configuración previa en GitHub

**Secrets** (Settings → Secrets and variables → Actions):

| Secret | Valor |
|---|---|
| `AWS_ACCESS_KEY_ID` | Credencial AWS usada por Actions |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS usada por Actions |
| `AWS_SESSION_TOKEN` | Token temporal de AWS Academy; se renueva al reiniciar el lab |
| `EC2_HOST` | IP pública de la instancia EC2 |
| `EC2_SSH_KEY` | Contenido completo del `.pem`, incluyendo `BEGIN`/`END`; pegar sin comillas ni espacios extra |

**Variables** (pestaña *Variables*):

| Variable | Valor |
|---|---|
| `AWS_REGION` | `us-east-1` |
| `S3_BUCKET` | `veterinaria-san-marcos-frontend` |
| `EC2_INSTANCE_ID` | `i-0ce5ae680699b5b2a` (referencia; ya no se usa en el deploy) |
| `VITE_API_URL` | `http://<IP>/api/v1` (o la URL de la Elastic IP) |

Permisos: el usuario IAM usado por Actions necesita `s3:PutObject` y `s3:DeleteObject`. El deploy ya no usa `ssm:SendCommand` ni `ec2:DescribeInstances`, y la EC2 no necesita `AmazonSSMManagedInstanceCore` para recibir el reinicio. El Security Group de la instancia debe permitir tráfico entrante `SSH / TCP 22`; para la demo se puede usar origen `0.0.0.0/0`, y en producción conviene restringirlo.

Credenciales de AWS Academy: al iniciar un nuevo lab cambian los tres secrets de AWS (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`). Las claves `ASIA...` son temporales; GitHub no las renueva automáticamente.

## 10. Logs en AWS (CloudWatch)

- En la instancia: `sudo journalctl -u sanmarcos -n 100 --no-pager`.
- El servicio escribe además a `/var/log/sanmarcos/application.log` (variable `LOG_FILE` en `/etc/sanmarcos.env`).
- CloudWatch agrupa los logs en `/aws/sanmarcos/backend` (aplicación) y `/aws/sanmarcos/heartbeat` (latidos), con retención de 14 días.

Instalación del agente (una sola vez, dentro de la instancia):

1. Adjuntar al rol de la instancia la política `CloudWatchAgentServerPolicy` (la retención de 14 días requiere además `logs:PutRetentionPolicy`). Esta política es solo para CloudWatch Logs; el deploy por SSH no la necesita.
2. Copiar la carpeta `aws/` del repo a la instancia y ejecutar:

   ```bash
   echo 'LOG_FILE=/var/log/sanmarcos/application.log' | sudo tee -a /etc/sanmarcos.env
   sudo systemctl restart sanmarcos
   cd /ruta/al/repo/sanmarcos/aws
   sudo bash setup-monitoring.sh
   ```

3. Verificar desde tu PC:

   ```powershell
   aws logs tail /aws/sanmarcos/backend --since 30m --follow
   ```

Consulta útil en CloudWatch → Logs Insights:

```
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc
| limit 20
```

## 11. Ping y health

Dos niveles distintos, ambos públicos (permitidos en `SecurityConfig`):

```bash
curl http://<IP>/api/v1/ping          # pong → el proceso está vivo
curl http://<IP>/actuator/health      # {"status":"UP", ...} → BD, disco y estado de la app
curl http://<IP>/actuator/health/liveness
curl http://<IP>/actuator/health/readiness
```

`actuator` se agregó en `pom.xml` y se configuró en `application.yml`. El indicador `db` usa el `DataSource`, así que un `DOWN` significa que MySQL no responde. No exponer `show-details: always` sin autenticación: el valor actual es `when_authorized`.

## 12. Heartbeat y alarma

`aws/heartbeat.sh` consulta `/actuator/health` cada minuto mediante un timer de systemd y escribe
`HEARTBEAT OK` o `HEARTBEAT FAIL` en `/var/log/sanmarcos/heartbeat.log`.

```bash
sudo systemctl status sanmarcos-heartbeat.timer
sudo journalctl -u sanmarcos-heartbeat.service -n 20 --no-pager
tail -f /var/log/sanmarcos/heartbeat.log
```

En CloudWatch → Logs → Create metric filter sobre `/aws/sanmarcos/heartbeat`:

- Filter pattern: `"HEARTBEAT OK"`
- Metric name: `SanMarcosHeartbeat`, value `1`, unit `Count`.

Luego crear una alarma: `Sum(SanMarcosHeartbeat) < 1` durante **5 minutos**, tratando los datos faltantes como *breaching*. Así se detecta tanto una caída del servicio como la del propio timer.

