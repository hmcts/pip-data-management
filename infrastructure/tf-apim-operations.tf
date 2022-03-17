locals {
  api_operations_files = fileset(path.module, "./resources/operation-policies/*.xml")
  api_operations = {
    for api_operations_file in local.api_operations_files :
    basename(api_operations_file) => {
      operation_id = replace(basename(api_operations_file), ".xml", "")
      xml_content  = file("${path.module}/${api_operations_file}")
    }
  }

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