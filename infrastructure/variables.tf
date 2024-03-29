## Defaults
variable "product" {
  default = "pip"
}
variable "component" {
  default = "data-management"
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
variable "aks_subscription_id" {
  default = ""
}
variable "jenkins_AAD_objectId" {}