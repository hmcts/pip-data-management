terraform {
  backend "azurerm" {
    resource_group_name  = "jenkins-state-sbox"
    storage_account_name = "sdsstatesbox"
    container_name       = "tfstate-sbox"
    key                  = "pip-publication-service-apim/sbox/terraform.tfstate"
  }
}
