locals {

  auto_secret_prefix  = "auto-${var.product}-${local.env}"
  resource_group_name = "${local.prefix}-${var.env}-rg"
  key_vault_name      = "${local.prefix}-kv-${var.env}"
}

data "azurerm_client_config" "current" {}


data "azurerm_subnet" "iaas" {
  name                 = "iaas"
  resource_group_name  = "ss-${var.env}-network-rg"
  virtual_network_name = "ss-${var.env}-vnet"
}

data "azurerm_key_vault" "kv" {
  name                = local.key_vault_name
  resource_group_name = local.resource_group_name
}


data "azurerm_api_management_product" "apim_product" {
  count               = local.deploy_apim
  product_id          = "${var.product}-product-${local.env}"
  resource_group_name = local.apim_rg
  api_management_name = local.apim_name
}

data "azurerm_key_vault_secret" "data_client_pwd" {
  name         = "app-pip-data-management-${var.env}-pwd"
  key_vault_id = data.azurerm_key_vault.kv.id
}
data "azurerm_key_vault_secret" "data_client_id" {
  name         = "app-pip-data-management-${var.env}-id"
  key_vault_id = data.azurerm_key_vault.kv.id
} 
data "azurerm_key_vault_secret" "data_client_app_url" {
  name         = "app-pip-data-management-${var.env}-app-id"
  key_vault_id = data.azurerm_key_vault.kv.id
} 