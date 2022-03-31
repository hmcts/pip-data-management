terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      version = ">=2.96.0"
    }
  }
}

provider "azurerm" {
  features {}
}

