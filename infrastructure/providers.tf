terraform {
  required_version = ">= 1.0.4"
  required_providers {
    azurerm = {
      version = "3.14"
    }
    postgresql = {
      source = "cyrilgdn/postgresql"
      version = ">=1.17.1"
    }
  }
}

provider "azurerm" {
  features {}
}

locals {
  db_name         = replace(var.component, "-", "")
  postgresql_user = "${local.db_name}_user"
}

provider "postgresql" {
  host            = module.database.host_name
  port            = module.database.postgresql_listen_port
  database        = module.database.postgresql_database
  username        = locals.postgresql_user
  password        = module.database.postgresql_password
  superuser       = false
  sslmode         = "require"
  connect_timeout = 15
}
