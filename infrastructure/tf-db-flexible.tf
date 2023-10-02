locals {
  db_host_name = "flexible-${var.product}-${var.component}"
}

module "postgresql" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source               = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  name                 = local.db_host_name
  product              = var.product
  component            = var.component
  location             = var.location
  env                  = var.env
  pgsql_admin_username = local.postgresql_user
  pgsql_databases = [
    {
      name : local.db_name
    }
  ]
  common_tags   = var.common_tags
  business_area = "sds"
  pgsql_version = "15"

  admin_user_object_id = var.jenkins_AAD_objectId

  pgsql_server_configuration = [
    {
     name  = "azure.extensions"
     value = "plpgsql, pg_stat_statements, pg_buffercache"
    },
    {
      name  = "backslash_quote"
      value = "on"
    }
  ]
}

# SDP access and MV required in here. Will be done at migration
