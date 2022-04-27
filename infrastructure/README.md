#  Publication and Information Data Management APIM API

This is the configuration for setting up the Publication and Information Data Management API Management API, Operations and Policies


## Swagger

The API and the Operations are deployed using Swagger/Open API importing.

Any new Operation for the API should be added to the Swagger definition `infrastructure\resources\swagger\api-swagger.json`

## API Policies

The policies for the APIs are located `infrastructure\resources\api-policy`
These policies will affect all of the actions for the Data Management API.

## Operation Policies

The policies for the Operations are located `infrastructure\resources\operation-policies`
These policies will affect all of the actions for the Data Management API Operations.

### Add new Policy
To add a new policy, create the new XML file within the source location with the name of the Operation ID.
This should be the Operation ID set in the Swagger Definition and the new file name.

The content should start by looking like this:
```XML
<policies>
    <inbound>
        <base />
    </inbound>
    <backend>
        <base />
    </backend>
    <outbound>
        <base />
    </outbound>
    <on-error>
        <base />
    </on-error>
</policies>
```

## Testing

Testing is done via the Java Gradle, which is located in `./src` and then the respective testing folder.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details


## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.0.4 |
| <a name="requirement_azurerm"></a> [azurerm](#requirement\_azurerm) | >=2.0.0 |       

## Providers

| Name | Version |
|------|---------|
| <a name="provider_azurerm"></a> [azurerm](#provider\_azurerm) | 2.99.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_apim_api"></a> [apim\_api](#module\_apim\_api) | git@github.com:hmcts/cnp-module-api-mgmt-api | master |
| <a name="module_apim_api_policy"></a> [apim\_api\_policy](#module\_apim\_api\_policy) | git@github.com:hmcts/cnp-module-api-mgmt-api-policy | master |

## Resources

| Name | Type |
|------|------|
| [azurerm_api_management_api_operation_policy.apim_api_operation_policy](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/api_management_api_operation_policy) | resource |
| [azurerm_api_management_product.apim_product](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/data-sources/api_management_product) | data source |
| [azurerm_key_vault.b2c](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/data-sources/key_vault) | data source |
| [azurerm_key_vault_secret.b2c_client_id](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/data-sources/key_vault_secret) | data source |
| [azurerm_key_vault_secret.b2c_tenant_id](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/data-sources/key_vault_secret) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_common_tags"></a> [common\_tags](#input\_common\_tags) | n/a | `map(string)` | n/a | yes |
| <a name="input_component"></a> [component](#input\_component) | n/a | `string` | `"sds"` | no |
| <a name="input_deployment_namespace"></a> [deployment\_namespace](#input\_deployment\_namespace) | n/a | `string` | `""` | no |
| <a name="input_env"></a> [env](#input\_env) | n/a | `any` | n/a | yes |
| <a name="input_location"></a> [location](#input\_location) | n/a | `string` | `"UK South"` | no |
| <a name="input_product"></a> [product](#input\_product) | # Defaults | `string` | `"pip"` | no |
| <a name="input_subscription"></a> [subscription](#input\_subscription) | n/a | `string` | `""` | no |
| <a name="input_team_contact"></a> [team\_contact](#input\_team\_contact) | n/a | `string` | `"#vh-devops"` | no |
| <a name="input_team_name"></a> [team\_name](#input\_team\_name) | n/a | `string` | `"PIP DevOps"` | no |

## Outputs

No outputs.