locals {
  secret_prefix = "${var.component}-POSTGRES"

  //Needed to change the old details to the new Flexible Server details, as Flyway on the pipeline only picks up
  //a specific naming convention.
  secrets = var.env == "sbox" || var.env == "demo" || var.env == "test" || var.env == "ithc" ? [
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
  ] : [
    {
      name_suffix = "PASS"
      value       = module.database.postgresql_password
    },
    {
      name_suffix = "HOST"
      value       = module.database.host_name
    },
    {
      name_suffix = "USER"
      value       = module.database.user_name
    },
    {
      name_suffix = "PORT"
      value       = module.database.postgresql_listen_port
    },
    {
      name_suffix = "DATABASE"
      value       = module.database.postgresql_database
    }
  ]

}


## Loop secrets
resource "azurerm_key_vault_secret" "secret" {
  for_each     = { for secret in local.secrets : secret.name_suffix => secret }
  key_vault_id = data.azurerm_key_vault.kv.id
  name         = "${local.secret_prefix}-${each.value.name_suffix}"
  value        = each.value.value
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.database
  ]
}

resource "azurerm_key_vault_secret" "sdp-host" {
  key_vault_id = data.azurerm_key_vault.sdp-kv.id
  name         = "${local.secret_prefix}-HOST"
  value        = module.database.host_name
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.database
  ]
}

resource "azurerm_key_vault_secret" "sdp-port" {
  key_vault_id = data.azurerm_key_vault.sdp-kv.id
  name         = "${local.secret_prefix}-PORT"
  value        = module.database.postgresql_listen_port
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.database
  ]
}

resource "azurerm_key_vault_secret" "sdp-database" {
  key_vault_id = data.azurerm_key_vault.sdp-kv.id
  name         = "${local.secret_prefix}-DATABASE"
  value        = module.database.postgresql_database
  tags = merge(var.common_tags, {
    "source" : "${var.component} PostgreSQL"
  })
  content_type    = ""
  expiration_date = timeadd(timestamp(), "8760h")

  depends_on = [
    module.database
  ]
}

