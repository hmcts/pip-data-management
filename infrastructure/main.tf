provider "azurerm" {
  features {}
}


data "azurerm_resource_group" "example" {
  name = "pip-ss-sbox-rg"
}

output "id" {
  value = data.azurerm_resource_group.example.id
}