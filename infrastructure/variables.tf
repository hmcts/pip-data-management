## Defaults
variable "product" {
  default = "pip"
}
variable "component" {
<<<<<<< HEAD
  default = "data-management"
=======
  default = "pip-data-management"
>>>>>>> master
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
