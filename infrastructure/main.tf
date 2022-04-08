locals {
  env = (var.env == "aat") ? "stg" : (var.env == "sandbox") ? "sbox" : "${(var.env == "perftest") ? "test" : "${var.env}"}"

  base_url = "${var.component}.${local.env_long_name}.platform.hmcts.net"

  apim_name     = "sds-api-mgmt-${local.env}"
  apim_rg       = "ss-${local.env}-network-rg"
  env_long_name = var.env == "sbox" ? "sandbox" : var.env == "stg" ? "staging" : var.env

  deploy_apim = local.env == "stg" || local.env == "sbox" ? 1 : 0
}

