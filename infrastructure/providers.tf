terraform {
  required_version = ">= 1.0.4"
  required_providers {
    azurerm = {
      version = "3.75.0"
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
  host            = module.database.host_name
  port            = module.database.postgresql_listen_port
  database        = module.database.postgresql_database
  username        = module.database.user_name
  password        = module.database.postgresql_password
  superuser       = false
  sslmode         = "require"
  connect_timeout = 15
}
