<div align="center">

# 🚀 DevOps Project 03 — Java CI/CD with Jenkins + OpenShift


![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-Pipeline-red?logo=jenkins&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Multi--Stage-blue?logo=docker&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-Quality%20Gate-orange?logo=sonarqube&logoColor=white)
![OpenShift](https://img.shields.io/badge/OpenShift-Deployed-red?logo=redhatopenshift&logoColor=white)

> End-to-end Java Spring Boot deployment pipeline using Jenkins, Docker,
> SonarQube, Trivy, TruffleHog, and OpenShift.

</div>

---

## 📌 Table of Contents
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [CI/CD Pipeline Stages](#cicd-pipeline-stages)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
- [Jenkins Setup](#jenkins-setup)
- [SonarQube Setup](#sonarqube-setup)
- [OpenShift Deployment](#openshift-deployment)
- [Security Tools](#security-tools)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture

```
Developer
    │  git push
    ▼
GitHub Repository
    │  webhook / poll SCM
    ▼
Jenkins Pipeline
    ├─► Maven Build + Unit Tests
    ├─► JaCoCo Code Coverage
    ├─► TruffleHog (secret scanning)
    ├─► SonarQube (code quality + quality gate)
    ├─► Trivy FS Scan (dependency CVEs)
    ├─► Docker Build (multi-stage)
    ├─► Trivy Image Scan (OS + app CVEs)
    ├─► Docker Push → DockerHub
    └─► Deploy → OpenShift
            ├─ Deployment (rolling, zero-downtime)
            ├─ Service
            └─ Route (TLS)
                    │
                    ▼
            Smoke Test (health check)
                    │
                    ▼
            Slack Notification ✅ / ❌
```

---

## 🛠️ Tech Stack

| Category | Tool | Purpose |
|----------|------|---------|
| Language | Java 17 + Spring Boot 3.2 | Application |
| UI | Thymeleaf + HTML/CSS | Frontend |
| Build | Maven + JaCoCo | Build & coverage |
| CI/CD | Jenkins (Declarative Pipeline) | Automation |
| Secret Scan | TruffleHog | Detect leaked secrets |
| Code Quality | SonarQube | Static analysis |
| Security Scan | Trivy (FS + Image) | CVE scanning |
| Container | Docker (multi-stage) | Packaging |
| Orchestration | OpenShift | Deployment |
| Notifications | Slack | Alerts |

---

## 📁 Project Structure

```
DevOps-Project-03/
├── Jenkinsfile                         # Full 13-stage declarative pipeline
├── pom.xml                             # Maven build + JaCoCo + SonarQube
│
├── src/
│   ├── main/
│   │   ├── java/com/devops/app/
│   │   │   ├── Application.java
│   │   │   ├── controller/TaskController.java
│   │   │   ├── service/TaskService.java
│   │   │   ├── model/Task.java
│   │   │   └── repository/TaskRepository.java
│   │   └── resources/
│   │       ├── templates/index.html    # Thymeleaf UI
│   │       ├── static/css/style.css
│   │       ├── static/js/app.js
│   │       └── application.properties
│   └── test/
│
├── docker/
│   ├── Dockerfile                      # Multi-stage: Maven builder → JRE production
│   └── docker-compose.yml              # App + Jenkins + SonarQube + PostgreSQL
│
├── k8s/openshift/
│   ├── deployment.yaml                 # Rolling update, probes, resource limits
│   ├── service.yaml
│   └── route.yaml                      # OpenShift Route with TLS
│
├── sonarqube/
│   └── sonar-project.properties
│
└── .github/workflows/
    └── project-03-ci.yml               # GitHub Actions (paths-filtered)
```

---

## ⚙️ CI/CD Pipeline Stages

| # | Stage | Tool | What it does |
|---|-------|------|-------------|
| 1 | Checkout | Git | Pull source code |
| 2 | Build & Test | Maven | Compile + unit tests + JAR |
| 3 | Code Coverage | JaCoCo | Generate coverage report (min 70%) |
| 4 | Secret Scan | TruffleHog | Scan git history for leaked secrets |
| 5 | Code Analysis | SonarQube | Static analysis, bugs, smells |
| 6 | Quality Gate | SonarQube | Fail if quality below threshold |
| 7 | FS Scan | Trivy | Scan dependencies for CVEs |
| 8 | Docker Build | Docker | Multi-stage production image |
| 9 | Image Scan | Trivy | Scan image for CRITICAL CVEs |
| 10 | Docker Push | DockerHub | Push versioned image |
| 11 | Deploy | OpenShift | Rolling update, zero downtime |
| 12 | Smoke Test | curl | Health check after deploy |
| 13 | Notify | Slack | Success / failure alert |

---

## ✅ Prerequisites

```bash
# Required
java --version        # JDK 17+
mvn --version         # Maven 3.9+
docker --version      # Docker 24+
oc version            # OpenShift CLI

# Jenkins plugins needed:
# - Pipeline, Git, Docker Pipeline, SonarQube Scanner,
#   JaCoCo, Slack Notification, AnsiColor
```

---

## 💻 Local Setup

```bash
# 1. Clone and enter project
git clone https://github.com/lohithsampreeth/DevOps-Roadmap.git
cd DevOps-Roadmap/DevOps-Project-03

# 2. Build and run locally
mvn clean package
java -jar target/*.jar

# 3. Open app
open http://localhost:8080

# ── OR run full stack with Docker Compose ──────────────
docker-compose -f docker/docker-compose.yml up -d

# Services:
# App       → http://localhost:8080
# Jenkins   → http://localhost:8090   (admin / admin)
# SonarQube → http://localhost:9000   (admin / admin)
```

---

## 🔧 Jenkins Setup

```
1. Open http://localhost:8090
2. Install suggested plugins + these extra:
   - SonarQube Scanner
   - Docker Pipeline
   - JaCoCo
   - Slack Notification
   - AnsiColor

3. Global Tool Config → Add:
   - JDK: Name=JDK-17, JAVA_HOME=/usr/lib/jvm/java-17
   - Maven: Name=Maven-3.9, auto-install

4. Manage Jenkins → Credentials → Add:
   - dockerhub-credentials  (Username/Password)
   - sonar-token            (Secret Text)
   - openshift-token        (Secret Text)
   - slack-token            (Secret Text)

5. Manage Jenkins → Configure System → SonarQube:
   - Name: SonarQube
   - URL: http://sonarqube:9000
   - Token: (from SonarQube → My Account → Security)

6. New Item → Pipeline → SCM → Git:
   - Repo: https://github.com/lohithsampreeth/DevOps-Roadmap
   - Script Path: DevOps-Project-03/Jenkinsfile
```

---

## 🔍 SonarQube Setup

```
1. Open http://localhost:9000 (admin/admin → change password)
2. Create project: devops-task-manager
3. Generate token → save as Jenkins credential 'sonar-token'
4. Quality Gate used: Sonar way (default)
   - Coverage < 70% → FAIL
   - Bugs > 0 (blocker) → FAIL
   - Vulnerabilities > 0 → FAIL
```

---

## 🚀 OpenShift Deployment

```bash
# Login to OpenShift
oc login https://api.your-cluster.com:6443 --token=<your-token>

# Create project
oc new-project devops-project-03

# Apply manifests
oc apply -f k8s/openshift/deployment.yaml
oc apply -f k8s/openshift/service.yaml
oc apply -f k8s/openshift/route.yaml

# Watch rollout
oc rollout status deployment/task-manager -n devops-project-03

# Get app URL
oc get route task-manager -n devops-project-03

# Rollback
oc rollout undo deployment/task-manager -n devops-project-03
```

---

## 🛡️ Security Tools

### TruffleHog
Scans entire git history for verified secrets (API keys, passwords, tokens).
```bash
# Run manually
docker run --rm -v "$PWD:/repo" trufflesecurity/trufflehog:latest \
  git file:///repo --only-verified
```

### Trivy
Scans filesystem (dependencies) and Docker image for CVEs.
```bash
# Filesystem scan
trivy fs --severity HIGH,CRITICAL .

# Image scan
trivy image lohithsampreeth/devops-task-manager:latest
```

---

## 🔧 Troubleshooting

**Jenkins can't connect to Docker?**
```bash
# On Jenkins host, give jenkins user docker access
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

**SonarQube Quality Gate times out?**
```bash
# Check SonarQube is running and token is correct
curl http://localhost:9000/api/system/status
```

**OpenShift pod CrashLoopBackOff?**
```bash
oc logs deployment/task-manager -n devops-project-03
oc describe pod -l app=task-manager -n devops-project-03
```

---

## 📜 License
MIT © [Lohith Sampreeth](https://github.com/lohithsampreeth)

---
<div align="center">⭐ Star this repo if it helped you!</div>
