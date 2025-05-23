terraform {
  required_version = ">= 1.9.0"
  required_providers {
    azurerm = {
      version = "4.27.0"
    }
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = ">=1.17.1"
    }
  }
}

provider "azurerm" {
  features {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "postgres_network"
  subscription_id            = var.aks_subscription_id
}

provider "postgresql" {
  alias = "postgres-flexible"

  host            = module.postgresql.fqdn
  port            = 5432
  database        = local.db_name
  username        = module.postgresql.username
  password        = module.postgresql.password
  superuser       = false
  sslmode         = "require"
  connect_timeout = 15
}
