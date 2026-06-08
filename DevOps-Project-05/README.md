<div align="center">

# 🚀 DevOps Project 05 — Multi-Cloud Microservices Platform

![CI](https://img.shields.io/github/actions/workflow/status/lohithsampreeth/DevOps-Roadmap/project-05-ci.yml?branch=main&label=CI%2FCD&logo=githubactions)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Kafka](https://img.shields.io/badge/Kafka-3.5-black?logo=apachekafka)
![Istio](https://img.shields.io/badge/Istio-Service%20Mesh-466BB0?logo=istio)
![ELK](https://img.shields.io/badge/ELK-Stack-005571?logo=elastic)
![Terraform](https://img.shields.io/badge/Terraform-Multi--Cloud-7B42BC?logo=terraform)
![AWS](https://img.shields.io/badge/AWS-EKS-orange?logo=amazonaws)
![Azure](https://img.shields.io/badge/Azure-AKS-blue?logo=microsoftazure)

> Senior-level multi-cloud microservices platform with event-driven architecture.
> 4 Java Spring Boot services communicating via Apache Kafka,
> secured by Istio mTLS, observed via ELK Stack,
> deployed on AWS EKS + Azure AKS via Terraform.

</div>

---

## 📌 Table of Contents
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Microservices](#microservices)
- [Event Flow (Kafka)](#event-flow-kafka)
- [Istio Service Mesh](#istio-service-mesh)
- [ELK Stack Logging](#elk-stack-logging)
- [Multi-Cloud Terraform](#multi-cloud-terraform)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup)
- [Deploy to AWS EKS](#deploy-to-aws-eks)
- [Deploy to Azure AKS](#deploy-to-azure-aks)
- [CI/CD Pipeline](#cicd-pipeline)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture

```
                        ┌──────────────────────────────────────┐
  Client                │         Istio Ingress Gateway        │
  ────────────────────► │  (TLS termination + routing rules)   │
                        └────────────┬─────────────────────────┘
                                     │
                     ┌───────────────▼───────────────┐
                     │          API Gateway           │
                     │  (Spring Cloud Gateway)        │
                     │  Rate Limiting | Circuit Breaker│
                     └──┬────────────┬──────────┬────┘
                        │ mTLS       │ mTLS     │ mTLS
              ┌─────────▼──┐  ┌──────▼───┐  ┌──▼──────────────┐
              │   Order    │  │Inventory │  │  Notification   │
              │  Service   │  │  Service │  │    Service      │
              └─────┬──────┘  └────┬─────┘  └────────┬────────┘
                    │              │                   │
                    └──────────────▼───────────────────┘
                              Apache Kafka
                         (order/inventory/notification topics)
                                   │
                    ┌──────────────┴──────────────────┐
                    │         Logstash                 │
                    │  (parse + enrich + route logs)   │
                    └──────────────┬──────────────────┘
                                   │
                    ┌──────────────▼──────────────────┐
                    │       Elasticsearch              │
                    │  microservices-logs-* index      │
                    └──────────────┬──────────────────┘
                                   │
                    ┌──────────────▼──────────────────┐
                    │           Kibana                 │
                    │  Dashboards + Alerts             │
                    └─────────────────────────────────┘

Multi-Cloud Deployment:
  AWS  → Terraform → EKS Cluster (ap-south-1)
  Azure→ Terraform → AKS Cluster (East US)
```

---

## 🛠️ Tech Stack

| Category | Tool | Purpose |
|----------|------|---------|
| Services | Java 17 + Spring Boot 3.2 | 4 microservices |
| API Gateway | Spring Cloud Gateway | Rate limiting, circuit breaker, routing |
| Messaging | Apache Kafka 3.5 (Strimzi) | Event-driven async communication |
| Service Mesh | Istio | mTLS, canary deploys, circuit breaker, observability |
| Logging | ELK Stack (Elastic 8.11) | Centralized log aggregation + search |
| IaC (AWS) | Terraform → EKS + MSK + ECR | AWS infrastructure |
| IaC (Azure) | Terraform → AKS + ACR + Log Analytics | Azure infrastructure |
| CI/CD | GitHub Actions | Matrix builds, parallel service pipelines |
| Secret Scan | TruffleHog | Prevent credential leaks |
| CVE Scan | Trivy | Image + FS vulnerability scanning |
| Containers | Docker (multi-stage) | Production-grade images |

---

## 📁 Project Structure

```
DevOps-Project-05/
│
├── services/
│   ├── api-gateway/            # Spring Cloud Gateway (port 8080)
│   ├── order-service/          # Order CRUD + Kafka producer (port 8081)
│   ├── inventory-service/      # Inventory + Kafka consumer (port 8082)
│   ├── notification-service/   # Kafka consumer → notifications (port 8083)
│   └── Dockerfile.shared       # Shared multi-stage Dockerfile
│
├── terraform/
│   ├── aws/main.tf             # EKS + VPC + ECR + MSK (Kafka)
│   └── azure/main.tf           # AKS + ACR + Log Analytics Workspace
│
├── istio/
│   ├── gateway/gateway.yaml          # TLS ingress + host routing
│   ├── virtualservice/               # Canary traffic split, retries, timeouts
│   ├── destinationrule/              # Circuit breaker + mTLS + load balancing
│   └── peerauthentication/           # Enforce STRICT mTLS namespace-wide
│
├── kafka/
│   ├── topics/kafka-setup.sh         # Create all topics with retention policies
│   └── kafka-k8s.yaml               # Strimzi Kafka CR (3-broker HA cluster)
│
├── elk/
│   ├── logstash/pipeline/            # Parse logs from all services + Kafka events
│   ├── logstash/config/              # Logstash settings
│   └── elasticsearch/setup.sh        # ILM policy + index templates
│
├── k8s/
│   └── base/all-services.yaml        # K8s Deployments + Services + ConfigMap
│
├── docker-compose.yml                # Full local stack
│
└── .github/workflows/
    └── project-05-ci.yml             # Matrix build: all 4 services in parallel
```

---

## 🔌 Microservices

| Service | Port | Kafka Role | Description |
|---------|------|-----------|-------------|
| api-gateway | 8080 | — | Routes, rate-limits, circuit-breaks |
| order-service | 8081 | **Producer** → `order-events` | CRUD orders, publishes events |
| inventory-service | 8082 | **Consumer** ← `order-events` | Reserves/releases stock on events |
| notification-service | 8083 | **Consumer** ← `order-events` | Sends email/SMS notifications |

---

## 📨 Event Flow (Kafka)

```
1. POST /api/orders            → API Gateway
2. API Gateway                 → Order Service
3. Order Service creates order → publishes ORDER_CREATED to Kafka
4. Inventory Service (consumer) reads event → reserves stock
5. Notification Service (consumer) reads event → sends email
6. DELETE /api/orders/{id}     → Order Service → publishes ORDER_STATUS_UPDATED (CANCELLED)
7. Inventory Service           → releases reserved stock
8. Notification Service        → sends cancellation email
```

**Kafka Topics:**
- `order-events` — 3 partitions, 7-day retention
- `inventory-events` — 3 partitions
- `notification-events` — 3 partitions, 3-day retention
- `order-events-dlq` — Dead Letter Queue, 30-day retention

---

## 🕸️ Istio Service Mesh

| Feature | Config File | What it does |
|---------|-------------|-------------|
| mTLS | `peer-auth.yaml` | All service-to-service traffic encrypted (STRICT) |
| Canary deploy | `virtualservices.yaml` | 90% v1, 10% v2 traffic split for order-service |
| Circuit breaker | `destination-rules.yaml` | Eject pods after 5 consecutive 5xx errors |
| Retry | `virtualservices.yaml` | 3 retries with 3s timeout per attempt |
| Fault injection | `virtualservices.yaml` | Commented out — uncomment for chaos testing |

---

## 📊 ELK Stack Logging

```
Pod logs (JSON) → Filebeat DaemonSet → Logstash :5044
Kafka events    →                    → Logstash :9092 consumer
                                              │
                                    Enrich + Parse + Tag
                                              │
                                     Elasticsearch indices:
                                     microservices-logs-*
                                     microservices-errors-*
                                     kafka-events-*
                                              │
                                          Kibana :5601
                                     Dashboards + Alerts
```

**Kibana index patterns to create:**
- `microservices-logs-*` — all app logs
- `microservices-errors-*` — ERROR + FATAL logs only
- `kafka-events-*` — all Kafka event payloads

---

## ☁️ Multi-Cloud Terraform

| Cloud | Cluster | Registry | Managed Kafka |
|-------|---------|----------|---------------|
| AWS | EKS 1.29 (ap-south-1) | ECR | MSK (2 brokers) |
| Azure | AKS 1.29 (East US) | ACR | Self-managed Strimzi |

---

## ✅ Prerequisites

```bash
java --version          # 17+
mvn --version           # 3.9+
docker --version        # 24+
kubectl version         # 1.29+
terraform --version     # 1.7+
istioctl version        # 1.20+
aws --version           # v2 (for EKS)
az --version            # latest (for AKS)
helm version            # 3.14+
```

---

## 💻 Local Setup

```bash
git clone https://github.com/lohithsampreeth/DevOps-Roadmap.git
cd DevOps-Roadmap/DevOps-Project-05

# Start full stack (all services + Kafka + ELK)
docker-compose up -d

# Wait ~60s for everything to start, then:
# API Gateway  → http://localhost:8080
# Kafka UI     → http://localhost:8085
# Kibana       → http://localhost:5601
# Elasticsearch→ http://localhost:9200

# Setup Kafka topics
docker exec kafka bash /kafka-setup.sh

# Setup Elasticsearch
bash elk/elasticsearch/setup.sh

# Test the API
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PROD-001","quantity":2,"totalAmount":500,"customerId":"CUST-001"}'

# Watch Kafka events
docker exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-events --from-beginning
```

---

## 🚀 Deploy to AWS EKS

```bash
# 1. Provision EKS with Terraform
cd terraform/aws
terraform init && terraform apply -auto-approve
$(terraform output -raw configure_kubectl)

# 2. Install Istio
istioctl install --set profile=demo -y
kubectl label namespace devops-p05 istio-injection=enabled

# 3. Install Strimzi Kafka operator
kubectl create namespace kafka
kubectl apply -f https://strimzi.io/install/latest?namespace=kafka
kubectl apply -f kafka/kafka-k8s.yaml

# 4. Deploy ELK stack
kubectl create namespace elk
helm repo add elastic https://helm.elastic.co
helm install elasticsearch elastic/elasticsearch -n elk
helm install kibana elastic/kibana -n elk
helm install logstash elastic/logstash -n elk \
  --set logstashPipeline."microservices\.conf"="$(cat elk/logstash/pipeline/microservices.conf)"

# 5. Apply Istio configs
kubectl apply -f istio/peerauthentication/
kubectl apply -f istio/gateway/
kubectl apply -f istio/destinationrule/
kubectl apply -f istio/virtualservice/

# 6. Deploy all services
kubectl apply -f k8s/base/all-services.yaml

# 7. Watch pods
kubectl get pods -n devops-p05 -w
```

---

## 🔷 Deploy to Azure AKS

```bash
# 1. Provision AKS with Terraform
cd terraform/azure
terraform init && terraform apply -auto-approve
$(terraform output -raw configure_kubectl)

# 2. Install Istio (same as above)
istioctl install --set profile=demo -y

# 3. Build and push to ACR
ACR=$(terraform output -raw acr_login_server)
az acr login --name $ACR
for svc in api-gateway order-service inventory-service notification-service; do
  docker build -t $ACR/$svc:latest services/$svc/
  docker push $ACR/$svc:latest
done

# 4. Update image refs in k8s/base and apply
kubectl apply -f k8s/base/all-services.yaml
```

---

## ⚙️ CI/CD Pipeline

Matrix build — all 4 services in **parallel**:

```
Push to DevOps-Project-05/services/**
         │
         ├─► TruffleHog (repo-wide secret scan)
         │
         ├─► api-gateway:         Build → Trivy FS → Docker Push → Trivy Image
         ├─► order-service:       Build → Trivy FS → Docker Push → Trivy Image
         ├─► inventory-service:   Build → Trivy FS → Docker Push → Trivy Image
         └─► notification-service:Build → Trivy FS → Docker Push → Trivy Image
                  │
                  └─► Terraform Plan (AWS + Azure) — parallel
```

**GitHub Secrets needed:**

| Secret | Purpose |
|--------|---------|
| `DOCKER_USERNAME` / `DOCKER_PASSWORD` | DockerHub push |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Terraform AWS |
| `AZURE_CLIENT_ID` / `AZURE_CLIENT_SECRET` / `AZURE_SUBSCRIPTION_ID` / `AZURE_TENANT_ID` | Terraform Azure |

---

## 🔧 Troubleshooting

**Kafka consumer not receiving events?**
```bash
# Check consumer group lag
kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --describe --group inventory-service
```

**Istio mTLS blocking traffic?**
```bash
# Check mTLS status
istioctl x check-inject -n devops-p05
kubectl exec -n devops-p05 deploy/order-service \
  -c istio-proxy -- pilot-agent request GET stats | grep ssl
```

**Kibana showing no data?**
```bash
# Verify Logstash pipeline
curl http://localhost:9200/_cat/indices?v | grep microservices
# Check Logstash
docker logs logstash | tail -50
```

**Pods pending on AWS EKS?**
```bash
kubectl describe node
# Usually spot instance capacity — switch to ON_DEMAND in terraform
```

---

## 📜 License
MIT © [Lohith Sampreeth](https://github.com/lohithsampreeth)

---
<div align="center">⭐ Star this repo if it helped you!</div>
