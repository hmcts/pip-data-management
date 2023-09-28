module "postgresql" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source    = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  product   = var.product
  component = var.component
  location  = var.location
  env       = var.env
  pgsql_admin_username = local.postgresql_user
  pgsql_databases = [
    {
      name : local.db_name
    }
  ]
  common_tags        = var.common_tags
  business_area      = "sds"
  pgsql_version = "15"

  admin_user_object_id = var.jenkins_AAD_objectId
}

# SDP access and MV required in here. Will be done at migration
