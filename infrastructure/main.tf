locals {
  api_operations_files = fileset(path.module, "./resources/operation-policies/*.xml")
  api_operations = {
    for api_operations_file in local.api_operations_files :
    basename(api_operations_file) => {
      operation_id = replace(basename(api_operations_file), ".xml", "")
      xml_content  = file("${path.module}/${api_operations_file}")
    }
  }

  env = (var.env == "aat") ? "stg" : (var.env == "sandbox") ? "sbox" : "${(var.env == "perftest") ? "test" : "${var.env}"}"

  apim_name = "sds-api-mgmt-${local.env}"
  apim_rg   = "ss-${local.env}-network-rg"

  api_name = "${var.product}-data-management-api"
}

data "azurerm_api_management_product" "apim_product" {
  product_id          = "${var.product}-product-${local.env}"
  resource_group_name = local.apim_rg
  api_management_name = local.apim_name
}

module "apim_api" {
  source = "git@github.com:hmcts/cnp-module-api-mgmt-api?ref=master"

  api_mgmt_name  = local.apim_name
  api_mgmt_rg    = local.apim_rg
  display_name   = local.api_name
  name           = local.api_name
  path           = "${var.product}/data-management"
  product_id     = data.azurerm_api_management_product.apim_product.product_id
  protocols      = ["https"]
  revision       = "1"
  service_url    = var.service_url
  swagger_url    = file("./resources/swagger/api-swagger.json")
  content_format = "swagger-json"
}

module "apim_api_policy" {
  source                 = "git@github.com:hmcts/cnp-module-api-mgmt-api-policy?ref=master"
  api_mgmt_name          = local.apim_name
  api_mgmt_rg            = local.apim_rg
  api_name               = local.api_name
  api_policy_xml_content = file("./resources/api-policy/api-policy.xml")

  depends_on = [
    module.apim_api
  ]
}

resource "azurerm_api_management_api_operation_policy" "apim_api_operation_policy" {
  for_each            = { for operation in local.api_operations : operation.operation_id => operation }
  operation_id        = each.value.operation_id
  api_name            = local.api_name
  api_management_name = local.apim_name
  resource_group_name = local.apim_rg
  xml_content         = each.value.xml_content

  depends_on = [
    module.apim_api
  ]
}
