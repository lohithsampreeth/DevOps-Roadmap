<div align="center">

# 🚀 DevOps Project 04 — GitOps on On-Prem Kubernetes

![CI](https://img.shields.io/github/actions/workflow/status/lohithsampreeth/DevOps-Roadmap/project-04-ci.yml?branch=main&label=CI%2FCD&logo=githubactions&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Helm](https://img.shields.io/badge/Helm-3.14-blue?logo=helm)
![ArgoCD](https://img.shields.io/badge/ArgoCD-GitOps-orange?logo=argo)
![Vault](https://img.shields.io/badge/Vault-Secrets-black?logo=vault)
![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-red?logo=prometheus)
![Ansible](https://img.shields.io/badge/Ansible-Provisioning-red?logo=ansible)

> Full GitOps pipeline for a Java Spring Boot Inventory Manager deployed on
> on-prem Kubernetes using Helm, ArgoCD, HashiCorp Vault, Prometheus + Grafana,
> and Ansible for cluster provisioning.

</div>

---

## 📌 Table of Contents
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [GitOps Flow](#gitops-flow)
- [Prerequisites](#prerequisites)
- [1. Provision Cluster with Ansible](#1-provision-cluster-with-ansible)
- [2. Deploy Vault + Setup Secrets](#2-deploy-vault--setup-secrets)
- [3. Install ArgoCD](#3-install-argocd)
- [4. Deploy App via ArgoCD](#4-deploy-app-via-argocd)
- [5. Monitoring with Prometheus + Grafana](#5-monitoring-with-prometheus--grafana)
- [CI/CD Pipeline](#cicd-pipeline)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture

```
Developer
    │  git push (app code)
    ▼
GitHub Actions (CI)
    ├─► Maven Build + Tests
    ├─► TruffleHog Secret Scan
    ├─► Trivy FS + Image Scan
    ├─► Docker Build + Push to DockerHub
    └─► Update image.tag in Helm values.yaml
                │
                │  git commit "chore: update image tag"
                ▼
        GitHub Repo (Helm values updated)
                │
                │  ArgoCD detects git diff (polls every 3 min)
                ▼
        ArgoCD Auto-Sync
                │
                │  helm upgrade --install
                ▼
        On-Prem Kubernetes Cluster
            ├─ inventory-prod namespace
            │     ├─ Deployment (2-8 pods via HPA)
            │     ├─ Service
            │     ├─ Ingress (NGINX)
            │     └─ Vault sidecar (inject secrets)
            │
            ├─ vault namespace
            │     └─ Vault (KV secrets for DB creds)
            │
            └─ monitoring namespace
                  ├─ Prometheus (scrapes /actuator/prometheus)
                  └─ Grafana (dashboards + alerts)
```

---

## 🛠️ Tech Stack

| Category | Tool | Purpose |
|----------|------|---------|
| App | Java 17 + Spring Boot 3.2 | Inventory Manager REST + UI |
| Build | Maven + JaCoCo | Compile, test, coverage |
| Container | Docker (multi-stage) | Production-grade image |
| Secret Scan | TruffleHog | Detect leaked credentials |
| CVE Scan | Trivy | FS + image vulnerability scanning |
| Helm | Helm 3.14 | K8s package manager |
| GitOps | ArgoCD | Auto-sync git → K8s |
| Secrets | HashiCorp Vault | Inject DB creds at runtime |
| Monitoring | Prometheus + Grafana | Metrics + alerting |
| Provisioning | Ansible | Configure K8s nodes |
| CI | GitHub Actions | Automated pipeline |

---

## 📁 Project Structure

```
DevOps-Project-04/
│
├── app/                            # Spring Boot application
│   ├── src/main/java/.../
│   │   ├── Application.java
│   │   ├── controller/ProductController.java
│   │   ├── service/ProductService.java
│   │   ├── model/Product.java
│   │   └── repository/ProductRepository.java
│   ├── src/main/resources/
│   │   ├── templates/index.html    # Thymeleaf UI
│   │   ├── static/css/style.css
│   │   └── application.properties
│   ├── Dockerfile                  # Multi-stage build
│   └── pom.xml
│
├── helm/inventory-app/             # Helm chart
│   ├── Chart.yaml
│   ├── values.yaml                 # Image tag updated by CI
│   └── templates/
│       ├── deployment.yaml         # Vault sidecar injector annotations
│       ├── service-ingress-hpa.yaml
│       └── _helpers.tpl
│
├── argocd/
│   ├── application.yaml            # ArgoCD Application (auto-sync)
│   └── project.yaml                # ArgoCD AppProject (RBAC)
│
├── vault/
│   ├── vault-setup.sh              # Init secrets + K8s auth
│   └── vault-k8s.yaml              # Vault Deployment + ServiceAccount
│
├── prometheus/
│   ├── prometheus.yaml             # Scrape configs
│   ├── alert-rules.yaml            # CPU/memory/error alerts
│   └── monitoring-stack.yaml       # Prometheus + Grafana K8s manifests
│
├── ansible/
│   ├── site.yml                    # Main playbook
│   ├── inventory/
│   │   ├── hosts.ini               # Node IPs
│   │   └── group_vars/all.yml      # Shared variables
│   └── roles/
│       ├── common/tasks/main.yml   # Helm, Vault CLI, ArgoCD CLI, Node Exporter
│       └── k8s/tasks/main.yml      # kubeadm, kubelet, cluster init
│
└── .github/workflows/
    └── project-04-ci.yml           # Build → Scan → Push → Update Helm tag
```

---

## 🔄 GitOps Flow

The key GitOps pattern in this project:

```
1. You push code changes to DevOps-Project-04/app/
2. GitHub Actions builds + scans + pushes new Docker image
3. GitHub Actions updates image.tag in helm/inventory-app/values.yaml
4. ArgoCD detects the values.yaml change in git (every 3 min)
5. ArgoCD runs: helm upgrade inventory-app ./helm/inventory-app
6. Kubernetes rolling-updates pods to new image
7. Zero manual kubectl apply needed!
```

---

## ✅ Prerequisites

```bash
# Local machine
ansible --version     # 2.16+
helm version          # 3.14+
kubectl version       # 1.29+

# Target servers (Ubuntu 22.04)
# 3 VMs: 1 master + 2 workers
# Min: 2 CPU, 4GB RAM each
# SSH key access configured
```

---

## 1. Provision Cluster with Ansible

```bash
cd DevOps-Project-04/ansible/

# Update node IPs
nano inventory/hosts.ini

# Test SSH connectivity
ansible all -i inventory/hosts.ini -m ping

# Run full provisioning (installs Docker, K8s, Helm, ArgoCD, Vault)
ansible-playbook -i inventory/hosts.ini site.yml -v

# Expected output:
# master  : kubeadm init + flannel CNI
# workers : join cluster
# all     : Node Exporter, Helm, ArgoCD CLI, Vault CLI
```

---

## 2. Deploy Vault + Setup Secrets

```bash
# Deploy Vault to cluster
kubectl apply -f vault/vault-k8s.yaml

# Wait for Vault pod
kubectl get pods -n vault -w

# Setup secrets, policies, K8s auth
export VAULT_ROOT_TOKEN=root
bash vault/vault-setup.sh

# Verify
kubectl exec -n vault deploy/vault -- vault kv get secret/inventory-app
```

---

## 3. Install ArgoCD

```bash
# Create namespace and install (if not done by Ansible)
kubectl create namespace argocd
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for ArgoCD to be ready
kubectl wait --for=condition=available deploy/argocd-server -n argocd --timeout=120s

# Get initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d

# Port-forward ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:443
# Open: https://localhost:8080  (admin / <password above>)

# Apply ArgoCD project + application
kubectl apply -f argocd/project.yaml
kubectl apply -f argocd/application.yaml
```

---

## 4. Deploy App via ArgoCD

```bash
# ArgoCD will auto-sync within 3 minutes
# Or force sync manually:
argocd login localhost:8080 --username admin --insecure
argocd app sync inventory-manager

# Check sync status
argocd app get inventory-manager

# Watch pods come up
kubectl get pods -n inventory-prod -w

# Get app URL
kubectl get ingress -n inventory-prod
```

---

## 5. Monitoring with Prometheus + Grafana

```bash
# Deploy monitoring stack
kubectl apply -f prometheus/monitoring-stack.yaml

# Access Prometheus
kubectl port-forward svc/prometheus -n monitoring 9090:9090
# Open: http://localhost:9090
# Try: http_server_requests_seconds_count{job="inventory-app"}

# Access Grafana
# NodePort already configured at port 32000
# Open: http://<node-ip>:32000  (admin / devops123)
# Import dashboard IDs:
#   4701  — JVM (Micrometer) dashboard
#   6417  — Kubernetes cluster overview
#   1860  — Node Exporter full
```

---

## ⚙️ CI/CD Pipeline

Triggered on push to `DevOps-Project-04/app/**`:

```
Push → Build & Test → TruffleHog → Trivy FS
     → Docker Build → Trivy Image → Docker Push
     → Update helm/values.yaml (image.tag = git SHA)
     → ArgoCD detects change → auto deploys
```

**GitHub Secrets required:**

| Secret | Value |
|--------|-------|
| `DOCKER_USERNAME` | DockerHub username |
| `DOCKER_PASSWORD` | DockerHub password |
| `GH_PAT` | GitHub Personal Access Token (repo write) |

---

## 🔧 Troubleshooting

**ArgoCD stuck in OutOfSync?**
```bash
argocd app sync inventory-manager --force
kubectl describe application inventory-manager -n argocd
```

**Vault secrets not injecting?**
```bash
kubectl logs -n inventory-prod \
  $(kubectl get pod -n inventory-prod -l app=inventory-app -o name | head -1) \
  -c vault-agent-init
```

**Pods not scheduling?**
```bash
kubectl describe pod -n inventory-prod -l app=inventory-app
kubectl get events -n inventory-prod --sort-by=.lastTimestamp
```

**Prometheus not scraping?**
```bash
# Check targets at http://localhost:9090/targets
# Ensure pod has annotation: prometheus.io/scrape: "true"
kubectl get pod -n inventory-prod -o yaml | grep prometheus
```

---

## 📜 License
MIT © [Lohith Sampreeth](https://github.com/lohithsampreeth)

---
<div align="center">⭐ Star this repo if it helped you!</div>
