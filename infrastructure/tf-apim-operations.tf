locals {
  operation_policies_files = fileset(path.module, "./resources/operation-policies/*.xml")
  operation_policies = local.deploy_apim == 0 ? {} : {
    for operation_policies_file in local.operation_policies_files :
    basename(operation_policies_file) => {
      operation_id = replace(basename(operation_policies_file), ".xml", "")
      xml_content = replace(replace(file("${path.module}/${operation_policies_file}"),
        "{BASE_URL}", local.base_url),
      "{TENANT_ID}", data.azurerm_client_config.current.tenant_id)
    }
  }

  operation_policies_files2 = fileset(path.module, "./resources2/operation-policies/*.xml")
  operation_policies2 = local.deploy_apim2 == 0 ? {} : {
    for operation_policies_file in local.operation_policies_files2 :
    basename(operation_policies_file) => {
      operation_id = replace(basename(operation_policies_file), ".xml", "")
      xml_content = replace(replace(file("${path.module}/${operation_policies_file}"),
        "{BASE_URL}", local.base_url),
        "{TENANT_ID}", data.azurerm_client_config.current.tenant_id)
    }
  }
}

resource "azurerm_api_management_api_operation_policy" "apim_api_operation_policy" {
  for_each            = { for operation in local.operation_policies : operation.operation_id => operation }
  operation_id        = each.value.operation_id
  api_name            = local.apim_api_name
  api_management_name = local.apim_name
  resource_group_name = local.apim_rg
  xml_content         = each.value.xml_content

  depends_on = [
    module.apim_api
  ]
}

resource "azurerm_api_management_api_operation_policy" "apim_api_operation_policy2" {
  for_each            = { for operation in local.operation_policies2 : operation.operation_id => operation }
  operation_id        = each.value.operation_id
  api_name            = local.apim_api_name2
  api_management_name = local.apim_name
  resource_group_name = local.apim_rg
  xml_content         = each.value.xml_content

  depends_on = [
    module.apim_api2
  ]
}
