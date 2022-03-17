locals {

  b2c_key_vault_name = "${var.product}-ss-apim-kv-${var.env}"
}

data "azurerm_api_management_product" "apim_product" {
  product_id          = "${var.product}-product-${local.env}"
  resource_group_name = local.apim_rg
  api_management_name = local.apim_name
}

data "azurerm_key_vault" "b2c" {
  name                = local.b2c_key_vault_name
  resource_group_name = "${var.product}-ss-${local.env}-rg"
}

data "azurerm_key_vault_secret" "b2c_tenant_id" {
  name         = "b2c-tenant-id"
  key_vault_id = data.azurerm_key_vault.b2c.id
}
data "azurerm_key_vault_secret" "b2c_client_id" {
  name         = "${var.product}-apim-data-managment-${var.env}"
  key_vault_id = data.azurerm_key_vault.b2c.id
}