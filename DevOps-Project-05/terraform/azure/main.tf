# ==============================================
# Terraform - Azure AKS Cluster (Project 05)
# ==============================================
terraform {
  required_version = ">= 1.7.0"
  required_providers {
    azurerm = { source = "hashicorp/azurerm", version = "~> 3.80" }
  }
  backend "azurerm" {
    resource_group_name  = "devops-p05-tfstate-rg"
    storage_account_name = "devopsp05tfstate"
    container_name       = "tfstate"
    key                  = "aks/terraform.tfstate"
  }
}

provider "azurerm" {
  features {}
}

variable "location"      { default = "East US" }
variable "project_name"  { default = "devops-p05" }
variable "cluster_name"  { default = "devops-p05-aks" }

resource "azurerm_resource_group" "main" {
  name     = "${var.project_name}-rg"
  location = var.location
}

resource "azurerm_kubernetes_cluster" "aks" {
  name                = var.cluster_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = var.cluster_name
  kubernetes_version  = "1.29"

  default_node_pool {
    name                = "system"
    node_count          = 2
    vm_size             = "Standard_D2s_v3"
    enable_auto_scaling = true
    min_count           = 1
    max_count           = 5
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin = "azure"
    network_policy = "calico"
  }

  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  }
}

# Additional node pool for apps
resource "azurerm_kubernetes_cluster_node_pool" "app" {
  name                  = "apppool"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.aks.id
  vm_size               = "Standard_D4s_v3"
  node_count            = 2
  enable_auto_scaling   = true
  min_count             = 1
  max_count             = 10
  node_labels           = { role = "app" }
}

resource "azurerm_container_registry" "acr" {
  name                = replace("${var.project_name}acr", "-", "")
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Standard"
  admin_enabled       = true
}

# Attach ACR to AKS
resource "azurerm_role_assignment" "aks_acr" {
  principal_id                     = azurerm_kubernetes_cluster.aks.kubelet_identity[0].object_id
  role_definition_name             = "AcrPull"
  scope                            = azurerm_container_registry.acr.id
  skip_service_principal_aad_check = true
}

resource "azurerm_log_analytics_workspace" "main" {
  name                = "${var.project_name}-logs"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
}

output "cluster_name"          { value = azurerm_kubernetes_cluster.aks.name }
output "acr_login_server"      { value = azurerm_container_registry.acr.login_server }
output "configure_kubectl"     {
  value = "az aks get-credentials --resource-group ${azurerm_resource_group.main.name} --name ${azurerm_kubernetes_cluster.aks.name}"
}
