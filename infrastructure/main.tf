locals {
  env = (var.env == "aat") ? "stg" : (var.env == "sandbox") ? "sbox" : "${(var.env == "perftest") ? "test" : "${var.env}"}"

  apim_name     = "sds-api-mgmt-${local.env}"
  apim_rg       = "ss-${local.env}-network-rg"
  env_long_name = var.env == "sbox" ? "sandbox" : var.env == "stg" ? "staging" : var.env

  deploy_apim = local.env == "stg" || local.env == "sbox" ? 1 : 0

  prefix              = "${var.product}-ss"
  prefix_no_special   = replace(local.prefix, "-", "")
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
