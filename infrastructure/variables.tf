## Defaults
variable "product" {
  default = "pip"
}
variable "component" {
  default = "pip-data-management"
}
variable "location" {
  default = "UK South"
}
variable "env" {}
variable "subscription" {
  default = ""
}
variable "deployment_namespace" {
  default = ""
}
variable "common_tags" {
  type = map(string)
}
