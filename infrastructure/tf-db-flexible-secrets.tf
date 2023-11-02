locals {
  flexible_secret_prefix = "${var.component}-POSTGRES-FLEXIBLE"

  flexible_secrets = [
    {
      name_suffix = "PASS"
      value       = module.postgresql.password
    },
    {
      name_suffix = "HOST"
      value       = module.postgresql.fqdn
    },
    {
      name_suffix = "USER"
      value       = module.postgresql.username
    },
    {
      name_suffix = "PORT"
      value       = "5432"
    },
    {
      name_suffix = "DATABASE"
      value       = local.db_name
    }
  ]

}

resource "azurerm_key_vault_secret" "flexible_secret" {
  for_each     = { for secret in local.flexible_secrets : secret.name_suffix => secret }
  key_vault_id = data.azurerm_key_vault.kv.id
  name         = "${local.flexible_secret_prefix}-${each.value.name_suffix}"
  value        = each.value.value
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.postgresql
  ]
}


# SDP Secrets also required which will be done at migration
resource "azurerm_key_vault_secret" "sdp-host-flexible" {
  key_vault_id = data.azurerm_key_vault.sdp-kv.id
  name         = "${local.flexible_secret_prefix}-HOST"
  value        = module.postgresql.fqdn
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.postgresql
  ]
}

resource "azurerm_key_vault_secret" "sdp-port-flexible" {
  key_vault_id = data.azurerm_key_vault.sdp-kv.id
  name         = "${local.flexible_secret_prefix}-PORT"
  value        = 5432
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.postgresql
  ]
}

resource "azurerm_key_vault_secret" "sdp-database-flexible" {
  key_vault_id = data.azurerm_key_vault.sdp-kv.id
  name         = "${local.flexible_secret_prefix}-DATABASE"
  value        = local.db_name
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.postgresql
  ]
}
