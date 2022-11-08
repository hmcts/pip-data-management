terraform {
  required_version = ">= 1.0.4"
  required_providers {
    azurerm = "3.14"
  }
}

provider "azurerm" {
  features {}
}

provider "postgresql" {
  host            = module.database.host_name
  port            = module.database.postgresql_listen_port
  database        = module.database.postgresql_database
  username        = module.database.user_name
  password        = module.database.postgresql_password
  sslmode         = "require"
  connect_timeout = 15
}
