module "application_insights" {
  source = "git@github.com:hmcts/terraform-module-application-insights?ref=4.x"

  env     = var.env
  name    = "${var.product}-${local.apim_name_prefix}"
  product = var.product

  resource_group_name = local.apim_rg
  sampling_percentage = var.sampling_percentage
  common_tags         = var.common_tags
  count               = local.env == "prod" ? 0 : 1
}

resource "azurerm_api_management_logger" "ai-logger" {
  name                = "${var.product}-${local.apim_name}-logger"
  api_management_name = local.apim_name
  resource_group_name = local.apim_rg
  count               = local.env == "prod" ? 0 : 1

  application_insights {
    connection_string = module.application_insights[0].connection_string
  }
}
