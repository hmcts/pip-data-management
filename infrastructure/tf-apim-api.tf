locals {
  apim_api_name       = "${var.product}-data-management-api"
  api_policy_raw = file("./resources/api-policy/api-policy.xml")
  api_policy = replace(replace(local.api_policy_raw
    , "{TENANT_ID}", "")
  , "{APP_CLIENT_ID}", "")
}
module "apim_api" {
  count  = local.deploy_apim
  source = "git@github.com:hmcts/cnp-module-api-mgmt-api?ref=master"

  api_mgmt_name         = local.apim_name
  api_mgmt_rg           = local.apim_rg
  display_name          = local.apim_api_name
  name                  = local.apim_api_name
  path                  = "${var.product}/data-management"
  product_id            = data.azurerm_api_management_product.apim_product[0].product_id
  protocols             = ["https"]
  revision              = "1"
  service_url           = "https://${local.base_url}"
  swagger_url           = file("./resources/swagger/api-swagger.json")
  content_format        = "openapi+json"
  subscription_required = false
}

/* module "apim_api_policy" {
  source                 = "git@github.com:hmcts/cnp-module-api-mgmt-api-policy?ref=master"
  api_mgmt_name          = module.apim_api[0].name
  api_mgmt_rg            = local.apim_rg
  api_name               = local.apim_api_name
  api_policy_xml_content = local.api_policy

  depends_on = [
    module.apim_api
  ]
} */