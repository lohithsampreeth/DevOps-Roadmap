# Jenkins Setup Guide for Project 03

## Required Jenkins Plugins

Install these from: Manage Jenkins → Plugin Manager

- Pipeline
- Git
- Maven Integration
- SonarQube Scanner
- JaCoCo
- Docker Pipeline
- OpenShift Client (oc)
- HTML Publisher
- Credentials Binding
- Timestamper
- Build Discarder

---

## Global Tool Configuration

Manage Jenkins → Global Tool Configuration:

### JDK
- Name: `JDK-17`
- JAVA_HOME: `/usr/lib/jvm/java-17-openjdk` (or install automatically)

### Maven
- Name: `Maven-3.9`
- Install automatically → version 3.9.x

### SonarQube Scanner
- Name: `SonarQube-Scanner`
- Install automatically → latest

---

## SonarQube Server Configuration

Manage Jenkins → Configure System → SonarQube servers:

- Name: `SonarQube-Server`   ← must match Jenkinsfile SONAR_SERVER
- URL: `http://localhost:9000` (or your SonarQube URL)
- Token: add via Jenkins credentials (type: Secret text, ID: `sonarqube-token`)

---

## Credentials Required

Manage Jenkins → Credentials → Global:

| ID | Type | Description |
|----|------|-------------|
| `docker-registry-credentials` | Username/Password | Docker registry login |
| `sonarqube-token` | Secret text | SonarQube auth token |
| `openshift-service-account-token` | Secret text | OpenShift SA token |

---

## Create the Pipeline Job

1. New Item → Pipeline → name it `DevOps-Project-03`
2. Pipeline → Definition: **Pipeline script from SCM**
3. SCM: Git → your repo URL
4. Script Path: `DevOps-Project-03/jenkins/Jenkinsfile`
5. Save → Build Now

---

## OpenShift Setup

```bash
# Create project
oc new-project devops-project-03

# Create secret for DB credentials
oc create secret generic devops-app-secret \
  --from-literal=DB_URL=jdbc:postgresql://your-db:5432/devopsdb \
  --from-literal=DB_USER=admin \
  --from-literal=DB_PASSWORD=yourpassword \
  -n devops-project-03

# Create service account for Jenkins
oc create serviceaccount jenkins-sa -n devops-project-03
oc adm policy add-role-to-user edit \
  system:serviceaccount:devops-project-03:jenkins-sa \
  -n devops-project-03

# Get token (add to Jenkins credentials)
oc sa get-token jenkins-sa -n devops-project-03
```

---

## Pipeline Flow Diagram

```
git push
   │
   ▼
Jenkins Triggers
   │
   ├─ 1. Checkout
   ├─ 2. TruffleHog (secret scan)     ← fails if secrets found
   ├─ 3. Maven Build (compile)
   ├─ 4. Unit Tests + JaCoCo Coverage
   ├─ 5. SonarQube Analysis
   ├─ 6. Quality Gate                 ← fails if coverage < 70%
   ├─ 7. Maven Package (JAR)
   ├─ 8. Docker Build
   ├─ 9. Trivy Scan                   ← fails if CRITICAL CVEs
   ├─ 10. Push to Registry
   ├─ 11. Deploy to OpenShift
   └─ 12. Smoke Test
         │
         ├─ SUCCESS → notify
         └─ FAILURE → auto rollback
```
