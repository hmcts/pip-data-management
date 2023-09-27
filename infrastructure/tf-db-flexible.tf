locals {
  db_name         = replace(var.component, "-", "")
  postgresql_user = "${local.db_name}_user"
}

module "postgresql" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  product       = var.product
  component     = var.component
#   subnet_id          = data.azurerm_subnet.iaas.id 
  location           = var.location
  env    = var.env
  pgsql_admin_username = local.postgresql_user
  pgsql_databases = [
    {
      name : ${local.db_name}
    }
  ]
  common_tags        = var.common_tags
    # subscription       = local.env_long_name
  business_area      = "SDS"
  pgsql_version = "14" # This is a switch from current at 11. Discussion needed
}

# SDP stuff needs to be here but will be covered by the migration workstream


