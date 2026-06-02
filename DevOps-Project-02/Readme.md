<div align="center">

# 🚀 DevOps CI/CD Pipeline Project

![CI/CD](https://img.shields.io/github/actions/workflow/status/yourusername/devops-cicd-project/ci.yml?label=CI%2FCD&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-24.x-blue?logo=docker&logoColor=white)
![Terraform](https://img.shields.io/badge/Terraform-1.7.x-purple?logo=terraform&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-1.29-326CE5?logo=kubernetes&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EKS%20%7C%20ECR%20%7C%20S3-orange?logo=amazonaws&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

> **End-to-end automated CI/CD pipeline** that builds, tests, security-scans,
> and deploys a containerized Node.js application to **AWS EKS** using
> GitHub Actions, Terraform, Docker, Kubernetes, Prometheus, and Grafana.

</div>

---

## 📌 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
- [Docker Setup](#docker-setup)
- [Terraform Infrastructure](#terraform-infrastructure)
- [Kubernetes Deployment](#kubernetes-deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Monitoring & Alerting](#monitoring--alerting)
- [Environment Variables](#environment-variables)
- [Scripts Reference](#scripts-reference)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## 📖 Overview

This project demonstrates a production-ready DevOps workflow with:

- **Automated CI/CD** via GitHub Actions (lint → test → build → scan → push → deploy)
- **Infrastructure as Code** using Terraform (AWS EKS, ECR, VPC, S3)
- **Container orchestration** with Kubernetes (HPA, rolling updates, zero downtime)
- **Security scanning** with Trivy on every build
- **Observability** with Prometheus metrics + Grafana dashboards
- **Automated alerts** via shell script (CPU/Memory/Disk/Services)
- **Config management** using Ansible playbooks

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Developer Workflow                 │
│  git push → GitHub → GitHub Actions (CI) → ECR      │
└───────────────────────────┬─────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────┐
│              GitHub Actions (CI Pipeline)            │
│  Lint → Unit Tests → Docker Build → Trivy Scan      │
│                        → Push to AWS ECR             │
└───────────────────────────┬─────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────┐
│              GitHub Actions (CD Pipeline)            │
│  kubectl apply → AWS EKS Cluster                    │
│    ├── Deployment (Rolling Update, Zero Downtime)   │
│    ├── HPA (Auto-scale 2 → 10 pods)                 │
│    ├── Service (ClusterIP)                          │
│    └── Ingress (NGINX + TLS)                        │
└───────────────────────────┬─────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────┐
│              Monitoring Stack                        │
│  Prometheus (scrape) → Grafana (visualize)          │
│  Node Exporter (host metrics)                       │
│  monitor.sh → Email/Slack Alerts                    │
└─────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category          | Tool                        | Version  |
|-------------------|-----------------------------|----------|
| CI/CD             | GitHub Actions               | Latest   |
| Containerization  | Docker (multi-stage build)   | 24.x     |
| Orchestration     | Kubernetes (AWS EKS)         | 1.29     |
| IaC               | Terraform                    | 1.7.x    |
| Cloud             | AWS (EKS, ECR, S3, VPC, IAM) | -        |
| Config Mgmt       | Ansible                      | 2.16     |
| Monitoring        | Prometheus + Grafana         | Latest   |
| Security Scan     | Trivy                        | Latest   |
| Ingress           | NGINX Ingress Controller     | Latest   |
| Scripting         | Bash                         | 5.x      |

---

## 📁 Project Structure

```
devops-cicd-project/
│
├── 📂 .github/
│   └── 📂 workflows/
│       ├── ci.yml              # Lint, Test, Docker Build, Trivy Scan, Push ECR
│       └── deploy.yml          # Deploy to AWS EKS
│
├── 📂 docker/
│   ├── Dockerfile              # Multi-stage production build (non-root, healthcheck)
│   └── docker-compose.yml      # Local: App + PostgreSQL + Redis + Prometheus + Grafana
│
├── 📂 terraform/
│   ├── main.tf                 # EKS Cluster, VPC, ECR, S3, DynamoDB lock
│   ├── variables.tf            # Input variables
│   └── outputs.tf              # Cluster endpoint, ECR URL, etc.
│
├── 📂 k8s/
│   ├── deployment.yaml         # Rolling update, probes, resource limits, topology spread
│   ├── service.yaml            # ClusterIP service
│   ├── ingress.yaml            # NGINX Ingress with TLS
│   ├── hpa.yaml                # Horizontal Pod Autoscaler (CPU + Memory)
│   └── configmap.yaml          # App environment config
│
├── 📂 ansible/
│   ├── playbook.yml            # Install Docker, kubectl, AWS CLI, Node Exporter
│   └── 📂 inventory/
│       └── hosts.ini           # Target server IPs
│
├── 📂 monitoring/
│   ├── prometheus-config.yaml  # Scrape configs for app + node-exporter + k8s
│   └── grafana-dashboard.json  # Pre-built Grafana dashboard
│
├── 📂 scripts/
│   ├── monitor.sh              # CPU/Memory/Disk/Service alerts → Email + Slack
│   ├── rollback.sh             # Emergency Kubernetes rollback
│   └── cleanup.sh              # Remove old Docker images + ECR tags
│
├── .env.example                # Template for environment variables
├── .gitignore                  # Excludes secrets, terraform state, node_modules
└── README.md
```

---

## ✅ Prerequisites

Install and configure the following tools:

```bash
# Verify all tools are installed
docker --version          # Docker 24+
kubectl version --client  # kubectl 1.29+
terraform --version       # Terraform 1.7+
aws --version             # AWS CLI v2
ansible --version         # Ansible 2.16+
helm version              # Helm 3.x
```

**AWS Setup:**
```bash
# Configure AWS credentials
aws configure
# Enter: Access Key, Secret Key, Region (ap-south-1), Output format (json)

# Verify
aws sts get-caller-identity
```

---

## 💻 Local Setup

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/devops-cicd-project.git
cd devops-cicd-project

# 2. Setup environment variables
cp .env.example .env
nano .env    # fill in your values

# 3. Install app dependencies
cd app && npm install && cd ..

# 4. Run tests
cd app && npm test
```

---

## 🐳 Docker Setup

```bash
# Build production image
docker build \
  -f docker/Dockerfile \
  -t devops-app:latest \
  .

# Run single container
docker run -d \
  -p 8080:8080 \
  --env-file .env \
  --name devops-app \
  devops-app:latest

# Check health
curl http://localhost:8080/health

# View logs
docker logs -f devops-app

# ── OR use Docker Compose (full stack) ──────────────────
docker-compose -f docker/docker-compose.yml up -d

# Check all services
docker-compose -f docker/docker-compose.yml ps

# View app logs only
docker-compose -f docker/docker-compose.yml logs -f app

# Stop everything
docker-compose -f docker/docker-compose.yml down -v
```

---

## 🏗️ Terraform Infrastructure

```bash
cd terraform/

# 1. Initialize (downloads AWS provider, sets up S3 backend)
terraform init

# 2. Validate configuration
terraform validate

# 3. Preview what will be created
terraform plan

# 4. Apply (creates EKS cluster, VPC, ECR, S3 — takes ~15 mins)
terraform apply -auto-approve

# 5. View outputs
terraform output
# Example output:
# cluster_name = "devops-cluster"
# ecr_repository_url = "123456789.dkr.ecr.ap-south-1.amazonaws.com/devops-cicd"
# configure_kubectl = "aws eks update-kubeconfig --region ap-south-1 --name devops-cluster"

# 6. Destroy when done (avoids AWS charges)
terraform destroy -auto-approve
```

---

## ☸️ Kubernetes Deployment

```bash
# 1. Connect kubectl to EKS cluster
aws eks update-kubeconfig \
  --region ap-south-1 \
  --name devops-cluster

# 2. Create namespace
kubectl create namespace devops-app

# 3. Create secret for DB credentials
kubectl create secret generic myapp-secret \
  --from-literal=DB_HOST=your-db-host \
  --from-literal=DB_PASSWORD=your-password \
  -n devops-app

# 4. Deploy all manifests
kubectl apply -f k8s/ -n devops-app

# 5. Watch pods come up
kubectl get pods -n devops-app -w

# 6. Check service & ingress
kubectl get svc,ingress -n devops-app

# 7. View pod logs
kubectl logs -f deployment/myapp -n devops-app

# 8. Check HPA status
kubectl get hpa -n devops-app

# 9. Manual scale (HPA handles auto)
kubectl scale deployment myapp --replicas=4 -n devops-app

# 10. Emergency rollback
bash scripts/rollback.sh devops-app myapp
```

---

## ⚙️ CI/CD Pipeline

The pipeline triggers automatically on every push to `main`:

```
git push to main
       │
       ├─► [CI] Checkout code
       ├─► [CI] npm lint (ESLint)
       ├─► [CI] npm test (Jest + coverage)
       ├─► [CI] Docker build (multi-stage)
       ├─► [CI] Trivy security scan (fail on CRITICAL/HIGH CVEs)
       ├─► [CI] Push image to AWS ECR
       │
       └─► [CD] kubectl apply k8s manifests
           └─► Rolling update (zero downtime)
               └─► Rollback automatically if deploy fails
```

**Required GitHub Secrets** (Settings → Secrets → Actions):

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | IAM access key |
| `AWS_SECRET_ACCESS_KEY` | IAM secret key |
| `AWS_REGION` | e.g. `ap-south-1` |
| `ECR_REPOSITORY` | Full ECR URL |
| `EKS_CLUSTER_NAME` | e.g. `devops-cluster` |

---

## 📊 Monitoring & Alerting

```bash
# ── Prometheus ─────────────────────────────────────────
kubectl port-forward svc/prometheus 9090:9090 -n monitoring
# Open: http://localhost:9090
# Try query: container_cpu_usage_seconds_total

# ── Grafana ────────────────────────────────────────────
kubectl port-forward svc/grafana 3000:3000 -n monitoring
# Open: http://localhost:3000
# Login: admin / admin123
# Import dashboard ID: 3119 (Kubernetes cluster monitoring)

# ── Run monitor.sh manually ────────────────────────────
bash scripts/monitor.sh
# Output:
# CPU: 23% ✓
# MEM: 61% ✓
# DISK /: 45% ✓
# SERVICE docker: running ✓

# ── Schedule monitor.sh via cron ───────────────────────
crontab -e
# Add: */5 * * * * /path/to/scripts/monitor.sh
```

---

## 🔐 Environment Variables

See `.env.example` for the full list. Key variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `APP_PORT` | Application port | `8080` |
| `NODE_ENV` | Runtime environment | `production` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `AWS_REGION` | AWS region | `ap-south-1` |
| `SLACK_WEBHOOK_URL` | Slack alert webhook | `https://hooks.slack.com/...` |

---

## 📜 Scripts Reference

| Script | Usage | Description |
|--------|-------|-------------|
| `monitor.sh` | `bash scripts/monitor.sh` | Checks CPU/MEM/Disk/Services, sends alerts |
| `rollback.sh` | `bash scripts/rollback.sh [namespace] [deployment]` | Rolls back K8s deployment |
| `cleanup.sh` | `bash scripts/cleanup.sh` | Removes old Docker images + ECR tags |

---

## 🔧 Troubleshooting

**Pods stuck in `CrashLoopBackOff`?**
```bash
kubectl describe pod <pod-name> -n devops-app
kubectl logs <pod-name> --previous -n devops-app
```

**Terraform state lock error?**
```bash
terraform force-unlock <LOCK_ID>
```

**AWS ECR login failing?**
```bash
aws ecr get-login-password --region ap-south-1 | \
  docker login --username AWS --password-stdin \
  <account-id>.dkr.ecr.ap-south-1.amazonaws.com
```

**Trivy scan blocking the build?**
```bash
# Run locally to see issues
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image devops-app:latest
```

**HPA not scaling?**
```bash
# Check metrics-server is running
kubectl get deployment metrics-server -n kube-system
kubectl top pods -n devops-app
```

---

## 🤝 Contributing

```bash
# 1. Fork the repo and clone
git clone https://github.com/yourusername/devops-cicd-project.git

# 2. Create feature branch
git checkout -b feature/add-alertmanager

# 3. Make changes and commit (conventional commits)
git commit -m "feat: add Alertmanager for Prometheus alerts"

# 4. Push and open Pull Request
git push origin feature/add-alertmanager
```

---

## 📜 License

MIT © [Your Name](https://github.com/yourusername)

---

<div align="center">

⭐ **Star this repo if it helped you!**

</div>
