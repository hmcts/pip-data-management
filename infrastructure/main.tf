locals {
  env = (var.env == "aat") ? "stg" : (var.env == "sandbox") ? "sbox" : "${(var.env == "perftest") ? "test" : "${var.env}"}"

  apim_name = "sds-api-mgmt-${local.env}"
  apim_rg   = "ss-${local.env}-network-rg"
}
