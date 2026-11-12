# Jenkins + SonarQube (local)

Corren en la laptop con Docker, solo cuando se necesitan. Costo: $0.

## Requisitos
- Docker Desktop con al menos **4 GB de RAM asignados** (SonarQube ~2 GB, Jenkins ~1 GB).
- En Windows, SonarQube necesita este ajuste una vez por reinicio de Docker Desktop (PowerShell):
  ```powershell
  wsl -d docker-desktop sysctl -w vm.max_map_count=262144
  ```

## 1. Levantar
```bash
docker compose -f infra/ci/docker-compose.ci.yml up -d --build
```
- Jenkins: http://localhost:8080
- SonarQube: http://localhost:9000

## 2. Configurar SonarQube (una sola vez)
1. Entrar con `admin` / `admin` y cambiar la contraseña.
2. **My Account → Security → Generate Token** (tipo *Global Analysis Token*). Copiar el token.
3. **Administration → Configuration → Webhooks → Create**
   - Name: `jenkins`
   - URL: `http://jenkins:8080/sonarqube-webhook/`
   (sin esto, la etapa *Quality Gate* se queda esperando)
4. **Quality Gates**: usar *Sonar way* o crear uno con cobertura ≥ 80% en código nuevo.

## 3. Configurar Jenkins (una sola vez)
1. Contraseña inicial:
   ```bash
   docker compose -f infra/ci/docker-compose.ci.yml exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
2. Instalar los plugins sugeridos y crear el usuario administrador.
3. **Manage Jenkins → Credentials → Global → Add**: tipo *Secret text*, ID `sonar-token`, valor = token de SonarQube.
4. **Manage Jenkins → System → SonarQube servers → Add**
   - Name: `sonarqube` (debe coincidir con el Jenkinsfile)
   - URL: `http://sonarqube:9000`
   - Token: `sonar-token`
5. **New Item → Multibranch Pipeline**
   - Branch source: GitHub, URL del repo `pni-hce`
   - Build configuration: *by Jenkinsfile*
   - Scan triggers: cada 5 minutos

Jenkins detecta `main`, `develop` y las `feature/*`, y ejecuta el `Jenkinsfile` en cada una.

## 4. Apagar
```bash
docker compose -f infra/ci/docker-compose.ci.yml down
```
Los datos (usuarios, jobs, historial de análisis) se conservan en volúmenes de Docker.

## Cobertura
Cada servicio debe incluir el plugin **JaCoCo** en su `pom.xml` desde el inicio; sin él, SonarQube reporta 0% de cobertura y el Quality Gate falla.
