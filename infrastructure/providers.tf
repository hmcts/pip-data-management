terraform {
  required_version = ">= 1.0.4"
  required_providers {
    azurerm = "3.14"
  }
}

provider "azurerm" {
  features {}
}
