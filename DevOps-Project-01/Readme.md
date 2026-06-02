<div align="center">

# 🚀 DevOps CI/CD Pipeline Project

![CI/CD](https://img.shields.io/github/actions/workflow/status/yourusername/devops-project/deploy.yml?label=CI%2FCD&logo=githubactions)
![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)
![Terraform](https://img.shields.io/badge/IaC-Terraform-purple?logo=terraform)
![Kubernetes](https://img.shields.io/badge/Orchestration-Kubernetes-326CE5?logo=kubernetes)
![License](https://img.shields.io/badge/License-MIT-green)

> End-to-end automated CI/CD pipeline that builds, tests, scans,
> and deploys a containerized Node.js app to AWS EKS using
> GitHub Actions, Terraform, Docker, and Kubernetes.

</div>

---

## 📌 Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
- [Docker Setup](#docker-setup)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Terraform Infrastructure](#terraform-infrastructure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Monitoring](#monitoring)
- [Environment Variables](#environment-variables)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## 🏗️ Architecture

```
Developer Push
      │
      ▼
GitHub Actions (CI)
  ├── Lint & Test
  ├── Docker Build
  ├── Trivy Security Scan
  └── Push to ECR
      │
      ▼
GitHub Actions (CD)
  └── kubectl apply → AWS EKS
        ├── Deployment (2-10 pods, HPA)
        ├── Service (LoadBalancer)
        └── Ingress (NGINX)
              │
              ▼
         Prometheus + Grafana
         (Monitoring & Alerting)
```

---

## 🛠️ Tech Stack

| Category | Tool | Version |
|----------|------|---------|
| CI/CD | GitHub Actions | Latest |
| Containerization | Docker | 24.x |
| Orchestration | Kubernetes (EKS) | 1.29 |
| IaC | Terraform | 1.7.x |
| Cloud | AWS (EKS, ECR, S3, VPC) | - |
| Monitoring | Prometheus + Grafana | - |
| Security Scan | Trivy | Latest |
| Config Mgmt | Ansible | 2.16 |

---

## 📁 Project Structure

```
devops-cicd-project/
│
├── 📂 .github/
│   └── 📂 workflows/
│       ├── ci.yml              # Build, test, scan
│       └── deploy.yml          # Deploy to EKS
│
├── 📂 app/                     # Application source
│   ├── src/
│   ├── tests/
│   └── package.json
│
├── 📂 docker/
│   ├── Dockerfile              # Multi-stage production build
│   └── docker-compose.yml      # Local dev environment
│
├── 📂 terraform/
│   ├── main.tf                 # AWS EKS + VPC + ECR
│   ├── variables.tf
│   ├── outputs.tf
│   └── backend.tf              # S3 remote state
│
├── 📂 k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── hpa.yaml
│   └── configmap.yaml
│
├── 📂 ansible/
│   ├── playbook.yml
│   └── inventory/
│
├── 📂 monitoring/
│   ├── prometheus-config.yaml
│   └── grafana-dashboard.json
│
├── 📂 scripts/
│   ├── monitor.sh              # Server health alerts
│   ├── cleanup.sh              # Remove old resources
│   └── rollback.sh             # Emergency rollback
│
└── README.md
```

---

## ✅ Prerequisites

Make sure you have the following installed:

```bash
# Check versions
docker --version          # Docker 24+
kubectl version           # kubectl 1.29+
terraform --version       # Terraform 1.7+
aws --version             # AWS CLI v2
ansible --version         # Ansible 2.16+
helm version              # Helm 3.x
```

Also required:
- AWS account with IAM user (AdministratorAccess for setup)
- DockerHub or AWS ECR account
- `kubectl` configured for EKS

---

## 💻 Local Setup

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/devops-cicd-project.git
cd devops-cicd-project

# 2. Copy environment variables
cp .env.example .env

# 3. Edit .env with your values
nano .env

# 4. Install app dependencies
cd app && npm install && cd ..

# 5. Run tests
cd app && npm test
```

---

## 🐳 Docker Setup

```bash
# Build the image
docker build -t devops-app:latest -f docker/Dockerfile .

# Run locally
docker run -p 8080:8080 --env-file .env devops-app:latest

# Or use Docker Compose (app + DB + Prometheus)
docker-compose -f docker/docker-compose.yml up -d

# Check running containers
docker ps

# View logs
docker logs -f devops-app

# Stop everything
docker-compose -f docker/docker-compose.yml down
```

---

## ☸️ Kubernetes Deployment

```bash
# 1. Connect to EKS cluster
aws eks update-kubeconfig \
  --region ap-south-1 \
  --name devops-cluster

# 2. Create namespace
kubectl create namespace devops-app

# 3. Apply all manifests
kubectl apply -f k8s/ -n devops-app

# 4. Check pod status
kubectl get pods -n devops-app -w

# 5. Check service & get external IP
kubectl get svc -n devops-app

# 6. View logs
kubectl logs -f deployment/myapp -n devops-app

# 7. Scale manually (HPA handles auto-scaling)
kubectl scale deployment myapp --replicas=4 -n devops-app

# 8. Rollback if needed
kubectl rollout undo deployment/myapp -n devops-app
```

---

## 🏗️ Terraform Infrastructure

```bash
cd terraform/

# 1. Initialize (downloads providers, sets up S3 backend)
terraform init

# 2. Preview what will be created
terraform plan -var-file="vars.tfvars"

# 3. Apply infrastructure (~10-15 mins for EKS)
terraform apply -var-file="vars.tfvars" -auto-approve

# 4. View outputs (cluster endpoint, ECR URL etc.)
terraform output

# 5. Destroy when done (avoid AWS charges)
terraform destroy -var-file="vars.tfvars" -auto-approve
```

---

## ⚙️ CI/CD Pipeline

The pipeline runs automatically on every push to `main`:

```
Push to main
    │
    ├─► Checkout code
    ├─► Run lint (ESLint)
    ├─► Run unit tests (Jest)
    ├─► Build Docker image
    ├─► Trivy security scan (fail if CRITICAL CVEs)
    ├─► Push image to AWS ECR
    └─► Deploy to EKS via kubectl
```

**Required GitHub Secrets:**

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | AWS IAM access key |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM secret |
| `AWS_REGION` | e.g. `ap-south-1` |
| `ECR_REPOSITORY` | Your ECR repo URL |
| `KUBE_CONFIG` | Base64 encoded kubeconfig |

---

## 📊 Monitoring

```bash
# Access Prometheus (port-forward)
kubectl port-forward svc/prometheus 9090:9090 -n monitoring

# Access Grafana
kubectl port-forward svc/grafana 3000:3000 -n monitoring
# Default login: admin / admin

# Key dashboards imported:
# - Node Exporter (cluster metrics)
# - Kubernetes Pod metrics
# - Application custom metrics
```

---

## 🔐 Environment Variables

```env
# .env.example
APP_PORT=8080
NODE_ENV=production

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=appdb
DB_USER=admin
DB_PASSWORD=changeme

# AWS
AWS_REGION=ap-south-1
ECR_REPO=123456789.dkr.ecr.ap-south-1.amazonaws.com/myapp
```

---

## 🔧 Troubleshooting

**Pods in CrashLoopBackOff?**
```bash
kubectl describe pod <pod-name> -n devops-app
kubectl logs <pod-name> --previous -n devops-app
```

**Terraform state lock error?**
```bash
terraform force-unlock <LOCK_ID>
```

**Docker image too large?**
```bash
# Check image layers
docker history devops-app:latest
# Use multi-stage build (already done in Dockerfile)
```

**GitHub Actions failing on AWS auth?**
```bash
# Verify secrets are set correctly in:
# Repo → Settings → Secrets and variables → Actions
```

---

## 🤝 Contributing

```bash
# 1. Fork the repo
# 2. Create a feature branch
git checkout -b feature/add-monitoring

# 3. Commit with conventional commits
git commit -m "feat: add Grafana dashboard for pod metrics"

# 4. Push and open a PR
git push origin feature/add-monitoring
```

---

## 📜 License

MIT © [lohithsampreeth](https://github.com/lohithsampreeth)

---

<div align="center">
⭐ Star this repo if it helped you!
</div>
