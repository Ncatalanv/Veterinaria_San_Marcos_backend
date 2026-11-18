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
   aws s3 sync .\dist s3://veterinaria-san-marcos-frontend/ --delete
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
