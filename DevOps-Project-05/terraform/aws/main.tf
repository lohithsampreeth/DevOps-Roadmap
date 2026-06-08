# ==============================================
# Terraform - AWS EKS Cluster (Project 05)
# ==============================================
terraform {
  required_version = ">= 1.7.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
  backend "s3" {
    bucket         = "devops-p05-tfstate-aws"
    key            = "eks/terraform.tfstate"
    region         = "ap-south-1"
    encrypt        = true
    dynamodb_table = "terraform-lock"
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = { Project = "devops-project-05", ManagedBy = "Terraform", Cloud = "AWS" }
  }
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"
  name = "${var.project_name}-aws-vpc"
  cidr = "10.0.0.0/16"
  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]
  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_dns_hostnames = true
  public_subnet_tags  = { "kubernetes.io/cluster/${var.cluster_name}" = "shared", "kubernetes.io/role/elb" = 1 }
  private_subnet_tags = { "kubernetes.io/cluster/${var.cluster_name}" = "shared", "kubernetes.io/role/internal-elb" = 1 }
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"
  cluster_name                   = var.cluster_name
  cluster_version                = "1.29"
  vpc_id                         = module.vpc.vpc_id
  subnet_ids                     = module.vpc.private_subnets
  cluster_endpoint_public_access = true
  enable_irsa                    = true
  eks_managed_node_groups = {
    app = {
      desired_size = 2; min_size = 1; max_size = 8
      instance_types = ["t3.large"]; capacity_type = "SPOT"
    }
  }
}

resource "aws_ecr_repository" "services" {
  for_each             = toset(["api-gateway","order-service","inventory-service","notification-service"])
  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration { scan_on_push = true }
}

variable "aws_region"    { default = "ap-south-1" }
variable "project_name"  { default = "devops-p05" }
variable "cluster_name"  { default = "devops-p05-eks" }
variable "environment"   { default = "prod" }

output "cluster_name"    { value = module.eks.cluster_name }
output "configure_kubectl" { value = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}" }
output "ecr_urls"        { value = { for k,v in aws_ecr_repository.services : k => v.repository_url } }
